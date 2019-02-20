package jhs.lc.jmf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

public class BufferedImageVideoProducer {
	private final int width, height;
	private final float frameRate;
	private final String fileTypeDescriptor;
	
	public BufferedImageVideoProducer(int width, int height, float frameRate,
			String fileTypeDescriptor) {
		this.width = width;
		this.height = height;
		this.frameRate = frameRate;
		this.fileTypeDescriptor = fileTypeDescriptor;
	}

	public void writeToFile(File outFile, Iterator<BufferedImage> images) throws IOException, NoProcessorException, InterruptedException {
		String outUrl = outFile.toURI().toString();
		MediaLocator mediaLocator = createMediaLocator(outUrl);
		BufferedImageDataSource dataSource = new BufferedImageDataSource(images, width, height, frameRate);
		Processor p = Manager.createProcessor(dataSource);
		LocalControllerListener cl = new LocalControllerListener();
		p.addControllerListener(cl);
		p.configure();
	    if(!cl.waitForState(p, Processor.Configured)) {
	        throw new IllegalStateException("Failed to configure the processor.");
	    }
	    p.setContentDescriptor(new ContentDescriptor(this.fileTypeDescriptor));
	    TrackControl tcs[] = p.getTrackControls();
	    Format f[] = tcs[0].getSupportedFormats();
	    if (f == null || f.length <= 0) {
	        throw new IllegalStateException("The mux does not support the input format: " + tcs[0].getFormat());
	    }
	    tcs[0].setFormat(f[0]);
	    p.realize();
	    if(!cl.waitForState(p, Processor.Realized)) {
	        throw new IllegalStateException("Failed to realize the processor.");
	    }
	    
	    DataSink dsink;
	    if ((dsink = createDataSink(p, mediaLocator)) == null) {
	        throw new IllegalStateException("Failed to create a DataSink for the given output MediaLocator: " + mediaLocator);
	    }

	    LocalDataSyncListener dsl = new LocalDataSyncListener();
	    dsink.addDataSinkListener(dsl);
	    
        p.start();
        dsink.start();

        dsl.waitForCompletion();
	    try {
	        dsink.close();
	    } catch (Exception e) {	    	
	    }	    
	    p.removeControllerListener(cl);
	    dsink.removeDataSinkListener(dsl);

	}
	
	private DataSink createDataSink(Processor p, MediaLocator outML) {
		DataSource ds;
		if ((ds = p.getDataOutput()) == null) {
			throw new IllegalStateException("The processor does not have an output DataSource");
		}
		DataSink dsink;
		try {
			dsink = Manager.createDataSink(ds, outML);
			dsink.open();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return dsink;
	}
	
	
	private static MediaLocator createMediaLocator(String url) {
		MediaLocator ml;
		if (url.indexOf(":") > 0 && (ml = new MediaLocator(url)) != null)
			return ml;
		if (url.startsWith(File.separator)) {
			return new MediaLocator("file:" + url);
		} else {
			String file = "file:" + System.getProperty("user.dir") + File.separator + url;
			return new MediaLocator(file);
		}
	}
	
	private class LocalDataSyncListener implements DataSinkListener {
		private boolean fileDone = false;
		private boolean fileSuccess = false;

		public void waitForCompletion() throws InterruptedException {
			synchronized(this) {
				while(!this.fileDone) {
					this.wait();
				}
			}
		}
		
		@Override
		public void dataSinkUpdate(DataSinkEvent evt) {
			if (evt instanceof EndOfStreamEvent) {
				synchronized (this) {
					fileDone = true;
					fileSuccess = true;
					this.notifyAll();
				}
			} else if (evt instanceof DataSinkErrorEvent) {
				synchronized (this) {
					fileDone = true;
					fileSuccess = false;
					this.notifyAll();
				}
			}
		}		
	}

	private class LocalControllerListener implements ControllerListener {
	    private boolean stateTransitionOK = true;
	
		public boolean waitForState(Processor p, int state) {
			synchronized (this) {
				try {
					while (p.getState() < state && stateTransitionOK)
						this.wait();
				} catch (Exception e) {}
			}
			return stateTransitionOK;
		}

		@Override
		public void controllerUpdate(ControllerEvent evt) {
			if (evt instanceof ConfigureCompleteEvent ||
					evt instanceof RealizeCompleteEvent ||
					evt instanceof PrefetchCompleteEvent) {
				synchronized (this) {
					stateTransitionOK = true;
					this.notifyAll();
				}
			} else if (evt instanceof ResourceUnavailableEvent) {
				synchronized (this) {
					stateTransitionOK = false;
					this.notifyAll();
				}
			} else if (evt instanceof EndOfMediaEvent) {
				evt.getSourceController().stop();
				evt.getSourceController().close();
			}
		}		
	}
}
