package jhs.lc.jmf;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;

public class BufferedImageSourceStream implements PullBufferStream {
	private final Iterator<BufferedImage> iterator;
	private final Format format;
	
	public BufferedImageSourceStream(Iterator<BufferedImage> iterator, Format format) {
		this.iterator = iterator;
		this.format = format;
	}

	public BufferedImageSourceStream(Iterator<BufferedImage> iterator, int width, int height, float frameRate) {
		this.iterator = iterator;
		this.format = new VideoFormat(VideoFormat.JPEG, new Dimension(width, height), Format.NOT_SPECIFIED, Format.byteArray, frameRate);
	}

	@Override
	public boolean endOfStream() {
		return !this.iterator.hasNext();
	}

	@Override
	public ContentDescriptor getContentDescriptor() {
		return new ContentDescriptor(ContentDescriptor.RAW);
	}

	@Override
	public long getContentLength() {
		return 0;
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
	public Format getFormat() {
		return this.format;
	}

	@Override
	public void read(Buffer buffer) throws IOException {
		if(!this.iterator.hasNext()) {
			buffer.setEOM(true);
			buffer.setOffset(0);
			buffer.setLength(0);
			return;
		}
		BufferedImage image = this.iterator.next();
		byte[] bytes = getJpegBytes(image);
		buffer.setData(bytes);
        buffer.setOffset(0);
        buffer.setLength(bytes.length);
        buffer.setFormat(this.format);
        buffer.setFlags(buffer.getFlags() | Buffer.FLAG_KEY_FRAME);
	}

	@Override
	public boolean willReadBlock() {
		return false;
	}
	
	private static byte[] getJpegBytes(BufferedImage image) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", out);
		return out.toByteArray();
	}
}
