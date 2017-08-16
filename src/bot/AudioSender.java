package bot;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.dv8tion.jda.core.audio.AudioSendHandler;

public class AudioSender implements AudioSendHandler {
	AudioInputStream in;
	static byte[] audioBytes;
	double fps;
	int bytesPer20Ms;
	int start = 0;
	public void init() throws UnsupportedAudioFileException, IOException {
		in = AudioSystem.getAudioInputStream(new File("Haywyre - Endlessly.mp3"));
		int bytesPerFrame = in.getFormat().getFrameSize();
		if(bytesPerFrame == AudioSystem.NOT_SPECIFIED) bytesPerFrame = 1;
		
		int numBytes = 1024 * bytesPerFrame;
		
		audioBytes = new byte[numBytes];
		
		int totalFramesRead = 0;
		int numBytesRead = 0;
		int numFramesRead = 0;
		while((numBytesRead = in.read(audioBytes)) != -1) {
			numFramesRead = numBytesRead / bytesPerFrame;
			totalFramesRead += numFramesRead;
			
		}
		
		fps = in.getFormat().getFrameRate();
		int framesPer20Ms = (int)((double)fps * .02);
		bytesPer20Ms = (int)((double)fps * (double)bytesPerFrame * .02);
	}
	@Override
	public boolean canProvide() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public byte[] provide20MsAudio() {
		if(start >= audioBytes.length) start = 0;
		byte[] snippet = new byte[bytesPer20Ms];
		for(int i = start; i < start + bytesPer20Ms; i++) {
			snippet[i - start] = audioBytes[i];
		}
		start += bytesPer20Ms;
		return snippet;
	}

}
