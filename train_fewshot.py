import os
import torch
import torch.nn as nn
import torchaudio
from pathlib import Path
import torch.nn.functional as F
import numpy as np
import json

print("🔁 사용자 정의 키워드 기반 Few-shot KWS 학습 시작...")

class KeywordNet(nn.Module):
    def __init__(self):
        super().__init__()
        self.conv = nn.Sequential(
            nn.Conv2d(1, 16, kernel_size=3, stride=2),
            nn.ReLU(),
        )
        self.pool = nn.AdaptiveAvgPool2d((1, 1))
        self.fc = nn.Linear(16, 128)

    def forward(self, x):
        x = self.conv(x)
        x = self.pool(x)
        x = x.view(x.size(0), -1)
        return self.fc(x)

def extract_mel(filepath, target_length=232):
    waveform, sr = torchaudio.load(filepath)
    waveform = torchaudio.functional.resample(waveform, sr, 16000)
    mel = torchaudio.transforms.MelSpectrogram(sample_rate=16000, n_mels=40)(waveform)
    if mel.shape[-1] < target_length:
        pad = target_length - mel.shape[-1]
        mel = F.pad(mel, (0, pad))
    elif mel.shape[-1] > target_length:
        mel = mel[:, :, :target_length]
    return mel.unsqueeze(0)

def load_custom_data(base_path="data/custom"):
    X, y, label_map = [], [], {}
    file_paths = {}
    for idx, keyword_dir in enumerate(sorted(Path(base_path).glob("*"))):
        if keyword_dir.is_dir():
            keyword = keyword_dir.name
            label_map[idx] = keyword
            file_paths[keyword] = []
            for file in keyword_dir.glob("*.wav"):
                X.append(extract_mel(file))
                y.append(idx)
                file_paths[keyword].append(file)
    return torch.cat(X), torch.tensor(y), label_map, file_paths

model = KeywordNet()
criterion = nn.CrossEntropyLoss()
optimizer = torch.optim.Adam(model.parameters(), lr=1e-3)

X, y, label_map, file_paths = load_custom_data()
print(f"📁 사용자 키워드 {len(label_map)}개, 총 {len(y)}개 샘플 로드됨")

# 학습
for epoch in range(10):
    model.train()
    logits = model(X)
    loss = criterion(logits, y)
    optimizer.zero_grad()
    loss.backward()
    optimizer.step()
    print(f"Epoch {epoch+1} | Loss: {loss.item():.4f}")

# ✅ 키워드별 평균 벡터 저장
model.eval()
keyword_embeddings = {}

with torch.no_grad():
    for keyword, paths in file_paths.items():
        vecs = []
        for path in paths:
            mel = extract_mel(path)
            emb = model(mel).squeeze()
            vecs.append(emb)
        mean_vec = torch.stack(vecs).mean(dim=0)
        vec = mean_vec / mean_vec.norm()
        keyword_embeddings[keyword] = [vec.view(-1).tolist()]

# 모델 저장
torch.save({"model": model.state_dict()}, "fewshot_model.pt")
with open("label_map.json", "w", encoding="utf-8") as f:
    json.dump(keyword_embeddings, f)

print("✅ 모델 및 키워드 벡터 저장 완료 → fewshot_model.pt, label_map.json")
