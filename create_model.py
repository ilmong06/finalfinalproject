import torch
from torchaudio.models import Conformer
from torchaudio.transforms import MelSpectrogram
from torch import nn

class ConformerEncoder(nn.Module):
    def __init__(self, input_dim=80, encoder_dim=144, num_layers=4):
        super().__init__()
        self.mel_transform = MelSpectrogram(
            sample_rate=16000,
            n_fft=1024,
            hop_length=160,
            n_mels=input_dim
        )
        self.encoder = Conformer(
            input_dim=input_dim,
            num_heads=4,
            ffn_dim=encoder_dim * 4,
            num_layers=num_layers,
            depthwise_conv_kernel_size=31,
            dropout=0.1
        )
        self.pool = nn.AdaptiveAvgPool1d(1)

    def forward(self, waveform):
        mel = self.mel_transform(waveform)
        mel = mel.transpose(1, 2)
        lengths = torch.full(size=(mel.shape[0],), fill_value=mel.shape[1], dtype=torch.long)
        x, _ = self.encoder(mel, lengths)
        x = x.transpose(1, 2)
        x = self.pool(x).squeeze(-1)
        return x

model = ConformerEncoder()
torch.save({"model": model.state_dict()}, "fewshot_model.pt")
print("✅ Conformer 기반 fewshot_model.pt 저장 완료")
