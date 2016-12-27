import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/***********************************
 * 
 * @author John Baker
 *
 * This comment block was added just
 * to see if my Git repo is working.
 ***********************************/

public class App 
{
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
	}
	
	private JFrame frame;
    private JFrame frame2;
	private JLabel imageLabel;
    private JLabel imageLabel2;
	
	public static void main(String[] args) {
		App app = new App();
		app.initGUI();
		app.runMainLoop(args);
	}
	
	private void initGUI() {
		frame = new JFrame("Color Filtered");  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		frame.setSize(400,400);  
		imageLabel = new JLabel();
		frame.add(imageLabel);
		frame.setVisible(true);       

        frame2 = new JFrame("Enhanced Footage");  
	    frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	    frame2.setSize(400,400);  
	    imageLabel2 = new JLabel();
	    frame2.add(imageLabel2);
	    frame2.setVisible(true);       
}

	private void runMainLoop(String[] args) {
		ImageProcessor imageProcessor = new ImageProcessor();
		Mat webcamMatImage = new Mat();  
		Image tempImage;  
        Image tempImage2;  
		VideoCapture capture = new VideoCapture(1);
		
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH,320);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,240);

		if( capture.isOpened()){  
			while (true){  
				capture.read(webcamMatImage);  
				if( !webcamMatImage.empty() ){
				    // Perform processing on image
				    Pipeline imagePipeline = new Pipeline();
				    imagePipeline.setsource0(webcamMatImage);
				    imagePipeline.process();
				 
				    // Get contours for overlay
				    ArrayList<MatOfPoint> contourArray = imagePipeline.convexHullsOutput();

				    // Find the contour with the largest area
			        double targetArea = -1.0;
			        int theTarget = 0;
			        
			        for (int i = 0; i < contourArray.size(); i++) {
			            if (Imgproc.contourArea(contourArray.get(i)) > targetArea) {
			                targetArea = Imgproc.contourArea(contourArray.get(i));
			                theTarget = i;
			            }
			        }

				    // Apply contours to raw footage
                    Mat overlayedImage = new Mat();
                    overlayedImage = webcamMatImage.clone();
                    
                    Imgproc.drawContours(overlayedImage, imagePipeline.convexHullsOutput(), theTarget, new Scalar(0, 0, 255), 5); 

			        // Convert thresholded matrix to image
				    tempImage = imageProcessor.toBufferedImage(imagePipeline.hsvThresholdOutput());
				    				    
                    // Convert enhanced raw footage matrix to image
                    tempImage2 = imageProcessor.toBufferedImage(overlayedImage);

					// Associate the Image with the JLabel
					ImageIcon imageIcon = new ImageIcon(tempImage, "Captured video");
					imageLabel.setIcon(imageIcon);
					
                    ImageIcon imageIcon2 = new ImageIcon(tempImage2, "Captured video");
                    imageLabel2.setIcon(imageIcon2);
                    
					// Resize the window to fit the image
					frame.pack();
                    frame2.pack();
				}  
				else{  
					System.out.println(" -- Frame not captured -- Break!"); 
					break;  
				}
			}  
		}
		else{
			System.out.println("Couldn't open capture.");
		}
		
	}
}
