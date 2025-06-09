import os
import torch
import torch.nn as nn
import torch.nn.functional as F
import torchaudio
from torch.utils.data import Dataset, DataLoader
from pathlib import Path
import json
import numpy as np

class MatchboxNetEncoder(nn.Module):
    def __init__(self):
        super().__init__()
        self.conv = nn.Sequential(
            nn.Conv1d(1, 64, kernel_size=5, stride=2, padding=2),
            nn.BatchNorm1d(64),
            nn.ReLU(),
            nn.Conv1d(64, 128, kernel_size=5, stride=2, padding=2),
            nn.BatchNorm1d(128),
            nn.ReLU()
        )
        self.pool = nn.AdaptiveAvgPool1d(1)

    def forward(self, x):  # x: [B, 1, T]
        x = self.conv(x)   # [B, C, T']
        return x

class KeywordDataset(Dataset):
    def __init__(self, root_dir):
        self.root = Path(root_dir)
        self.samples = []
        self.label_map = {}
        for i, folder in enumerate(sorted(self.root.iterdir())):
            if folder.is_dir():
                self.label_map[folder.name] = i
                for wav in folder.glob("*.wav"):
                    self.samples.append((str(wav), folder.name))

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx):
        path, label = self.samples[idx]
        waveform, sr = torchaudio.load(path)
        waveform = torchaudio.functional.resample(waveform, sr, 16000)
        if waveform.shape[0] > 1:
            waveform = waveform.mean(dim=0, keepdim=True)  # mono

        target_length = 16000
        if waveform.shape[1] > target_length:
            waveform = waveform[:, :target_length]
        else:
            waveform = F.pad(waveform, (0, target_length - waveform.shape[1]))

        return waveform.squeeze(0), self.label_map[label]

def prototypical_loss(embeddings, targets, n_classes):
    prototypes = []
    for c in range(n_classes):
        indices = (targets == c).nonzero(as_tuple=True)[0]
        support = embeddings[indices]
        prototype = support.mean(dim=0)
        prototypes.append(prototype)
    prototypes = torch.stack(prototypes)
    dists = torch.cdist(embeddings, prototypes)
    preds = torch.argmin(dists, dim=1)
    acc = (preds == targets).float().mean()
    return F.cross_entropy(-dists, targets), acc

if __name__ == "__main__":
    dataset = KeywordDataset("data/custom")
    loader = DataLoader(dataset, batch_size=16, shuffle=True)
    model = MatchboxNetEncoder()
    optimizer = torch.optim.Adam(model.parameters(), lr=1e-3)

    for epoch in range(10):
        model.train()
        total_loss, total_acc = 0, 0
        for wavs, labels in loader:
            emb = model(wavs)
            emb = torch.mean(emb, dim=2)  # Adaptive pooling across time dim
            loss, acc = prototypical_loss(emb, labels, n_classes=len(set(labels.tolist())))
            optimizer.zero_grad()
            loss.backward()
            optimizer.step()
            total_loss += loss.item()
            total_acc += acc.item()
        print(f"Epoch {epoch+1}: loss={total_loss:.4f}, acc={total_acc/len(loader):.4f}")

    torch.save({"model": model.state_dict()}, "matchbox_model.pt")
