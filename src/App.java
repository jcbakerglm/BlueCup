import java.awt.Image;
//import java.awt.Point;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/********************************************************************
 * 
 * Things to do:
 * 
 * 1. Parameterize initGUI so that it can be used for both windows.
 * 2. Look for other ways to streamline and improve code.
 *
 ********************************************************************/
public class App 
{
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
	}
	
	private JFrame processedFrame;
    private JFrame augmentedFrame;
	private JLabel processedImageLabel;
    private JLabel augmentedImageLabel;
	
	public static void main(String[] args) {
		App app = new App();
		app.initGUI(); 
		app.runMainLoop(args);
	}
	
	private void initGUI() {
		processedFrame = new JFrame("Processed Image");  
		processedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		processedFrame.setSize(400,400);  
		processedImageLabel = new JLabel();
		processedFrame.add(processedImageLabel);
		processedFrame.setVisible(true);       

		augmentedFrame = new JFrame("Augmented Image");  
		augmentedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		augmentedFrame.setSize(400,400);  
		augmentedImageLabel = new JLabel();
	    augmentedFrame.add(augmentedImageLabel);
	    augmentedFrame.setVisible(true);       
	}

	private void runMainLoop(String[] args) {
		ImageProcessor imageProcessor = new ImageProcessor();
		Mat webcamMatImage = new Mat();  
		Image processedImage;  
        Image augmentedImage;  
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
			        int theTarget = -1;
			        
			        for (int i = 0; i < contourArray.size(); i++) {
			            if (Imgproc.contourArea(contourArray.get(i)) > targetArea) {
			                targetArea = Imgproc.contourArea(contourArray.get(i));
			                theTarget = i;
			            }
			        }

                    Mat overlayedImage = new Mat();
                    overlayedImage = webcamMatImage.clone();
			        
			        if (theTarget > -1) {
	                    // Target in within the frame; grab it
			            MatOfPoint currentContour = contourArray.get(theTarget);
	                    
			            // Place targeting dot in center of target
	                    float[] radius = new float[1];
	                    Point center = new Point();
	                    MatOfPoint2f currentContour2f = new MatOfPoint2f();
	                    currentContour.convertTo(currentContour2f, CvType.CV_32FC2);
	                    Imgproc.minEnclosingCircle(currentContour2f, center, radius);
                        Imgproc.circle(overlayedImage, center, 2, new Scalar(0, 255, 0), 2);

                        // Get X and Y for center of JLabel
                        int frameX = (augmentedFrame.getWidth() / 2);
                        int frameY = (augmentedFrame.getHeight() / 2);
                          
                        // Add sighting ring; Set color:
                        // Green = Within sight radius
                        // Yellow = Close to sight
                        // Red = Not close to sight
                        Point sightCenter = new Point(frameX, frameY);
                        Scalar sightColor;
                        
                        if ((Math.pow(center.x - frameX, (double) 2) + Math.pow(center.y - frameY, (double) 2)) < Math.pow(15, 2)) {
                            sightColor = new Scalar(0, 255, 0);
                        } else if ((Math.pow(center.x - frameX, (double) 2) + Math.pow(center.y - frameY, (double) 2)) < Math.pow(30, 2)) {
                            sightColor = new Scalar(0, 216, 255);
                        } else {
                            sightColor = new Scalar(0, 0, 255);
                        }
                        
                        Imgproc.circle(overlayedImage, sightCenter, 15, sightColor, 2);
			        }

				    // Apply contours to raw footage
                    //Imgproc.drawContours(overlayedImage, contourArray, theTarget, new Scalar(0, 0, 255), 5); 

			        // Convert thresholded matrix to image to display in JLabel
                    processedImage = imageProcessor.toBufferedImage(imagePipeline.hsvThresholdOutput());
				    				    
                    // Convert enhanced raw footage matrix to image to display in JLabel
                    augmentedImage = imageProcessor.toBufferedImage(overlayedImage);

					// Associate the Image with the JLabel
					ImageIcon processedImageIcon = new ImageIcon(processedImage, "Processed Image");
					processedImageLabel.setIcon(processedImageIcon);
					
                    ImageIcon augmentedImageIcon = new ImageIcon(augmentedImage, "Augmented Image");
                    augmentedImageLabel.setIcon(augmentedImageIcon);
                    
					// Resize the windows to fit the image
                    processedFrame.pack();
                    augmentedFrame.pack();
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
