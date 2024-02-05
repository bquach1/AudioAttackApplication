%% origin
clf;
clc;
clear all;
close all;

[audio,fs]=audioread('dog_test1.wav');
% [audio,fs]=audioread('E:\logcat\1220_1\gap\audio_hand_4.wav');
% [audio,fs]=audioread('E:\logcat\1220_1\chest_attach\audio_hand_4.wav');
% [audio,fs]=audioread('E:\logcat\1220_1\elbow\audio_hand_4.wav');
% [audio,fs]=audioread('E:\logcat\1220_1\elbow_attach\audio_hand_4.wav');
% 
% [audio,fs]=audioread('E:\logcat\1220_1\wear\audio_hand_4.wav');
% [audio,fs]=audioread('E:\logcat\1220_1\handhold\audio_hand_4.wav');

% [audio,fs]=audioread('E:\logcat\240111\test_elbow\audio_hand_4.wav');
% [audio,fs]=audioread('E:\code\mistLAB\recoder\VoiceProContinuous2\VoiceProContinuous2\app\src\main\res\chirp0k24k30s.wav');
% [audio,fs]=audioread('E:\logcat\1220_1\8\audio_hand_4.wav');


t=(0:length(audio)-1)/fs;

plot(t,audio);
channel_left = audio(:,1);
channel_left = channel_left';

% channel_right = audio(:,2);
% channel_right = channel_right';

N = length(channel_left);

y = fft(channel_left);
f = fs / N * (0:round(N/2) - 1);

% z = fft(channel_right);

% plot(t, channel_left)
figure;
plot(f, abs(y(1:round(N/2))));
set(gca,'XLim',[0 2500], 'FontSize',20);
set(gca,'YLim',[0 2], 'FontSize',20);
grid;

xlabel('Freq/Hz','FontSize',30);
ylabel('amp','FontSize',30);
% title('gap','FontSize',30);
% hold on;
% figure;
% plot(f, abs(y(1:round(N/2))));
% 
% set(gca,'XLim',[0 2500], 'FontSize',20);
% set(gca,'YLim',[0 2], 'FontSize',20);
% grid;
% legend("left", "right", 'FontSize',24);


figure;
dt = 1 / fs;
win = hamming(512);
windowsize = 256;
[S,F,T] = spectrogram(channel_left,windowsize,windowsize/4,[],fs);
pcolor(T,F,log10(abs(S))),shading flat,colorbar
xlim([0, 1]);
ylim([0, 4000]);
