package jhs.lc.jmf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.media.Duration;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

public class BufferedImageDataSource extends PullBufferDataSource {
	private final BufferedImageSourceStream[] streams;
	
	public BufferedImageDataSource(Iterator<BufferedImage> iterator, int width, int height, float frameRate) {
		BufferedImageSourceStream stream = new BufferedImageSourceStream(iterator, width, height, frameRate);
		this.streams = new BufferedImageSourceStream[] { stream };
	}

	@Override
	public PullBufferStream[] getStreams() {
		return this.streams;
	}

	@Override
	public void connect() throws IOException {
	}

	@Override
	public void disconnect() {
	}

	@Override
	public String getContentType() {
		return ContentDescriptor.RAW;
	}

	@Override
	public Object getControl(String arg0) {
		return null;
	}

	@Override
	public Object[] getControls() {
		return new Object[0];
	}

	@Override
	public Time getDuration() {
		return Duration.DURATION_UNKNOWN;
	}

	@Override
	public void start() throws IOException {
	}

	@Override
	public void stop() throws IOException {
	}
}
