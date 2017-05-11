from __future__ import print_function
from imutils.video.pivideostream import PiVideoStream
from imutils.video import FPS
# import picamera
from picamera.array import PiRGBArray
from picamera import PiCamera
from collections import deque

import sys
import argparse
import imutils
import time
import cv2

#GPIO
import RPi.GPIO as GPIO
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(25,GPIO.OUT)


resolution=(640, 368)
Y_Uth = resolution[1]/3
Y_Lth = resolution[1]*2/3
Y_Mth = (Y_Uth+Y_Lth)/2
count = 0
flag = 0
X=0
Y=0

# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
# ap.add_argument("-n", "--num-frames", type=int, default=100, help="# of frames to loop over for FPS test")
# ap.add_argument("-d", "--display", type=int, default=1, help="Whether or not frames should be displayed")
# ap.add_argument("-v", "--video", help="path to the (optional) video file")
# ap.add_argument("-b", "--buffer", type=int, default=256, help="max buffer size")
args = vars(ap.parse_args())

# initialize the camera and grab a reference to the raw camera capture
camera = PiCamera()
camera.resolution = (640, 480)
camera.framerate = 120
rawCapture = PiRGBArray(camera, size=(640, 480))
stream = camera.capture_continuous(rawCapture, format="bgr", use_video_port=True)

stream.close()
rawCapture.close()
camera.close()

# define the lower and upper boundaries of the "green"
# ball in the HSV color space, then initialize the
# list of tracked points
# greenLower = (29, 86, 6)
# greenUpper = (64, 255, 255)
greenLower = (45, 80, 6)
greenUpper = (70, 255, 255)
pts = deque(maxlen=256)


framerate=120

vs = PiVideoStream(resolution, framerate).start()
time.sleep(2.0)
fps = FPS().start()

# capture frames from the camera
while fps._numFrames < 10000:
    # grab the raw NumPy array representing the image, then initialize the timestamp
    # and occupied/unoccupied text
    image = vs.read()


    # resize the frame, blur it, and convert it to the HSV
    # color space
    frame = image
    #blurred = cv2.GaussianBlur(frame, (11, 11), 0)
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)

    # construct a mask for the color "green", then perform
    # a series of dilations and erosions to remove any small
    # blobs left in the mask
    mask = cv2.inRange(hsv, greenLower, greenUpper)
    mask = cv2.erode(mask, None, iterations=2)
    mask = cv2.dilate(mask, None, iterations=2)
    # find contours in the mask and initialize the current
    # (x, y) center of the ball
    cnts = cv2.findContours(mask.copy(), cv2.RETR_EXTERNAL,
                            cv2.CHAIN_APPROX_SIMPLE)[-2]
    center = None
    # only proceed if at least one contour was found
    if len(cnts) > 0:
        # find the largest contour in the mask, then use
        # it to compute the minimum enclosing circle and
        # centroid
        c = max(cnts, key=cv2.contourArea)
        ((x, y), radius) = cv2.minEnclosingCircle(c)
        X=x
        Y=y

        M = cv2.moments(c)
        center = (int(M["m10"] / M["m00"]), int(M["m01"] / M["m00"]))

        # only proceed if the radius meets a minimum size
        if radius > 5:
            # draw the circle and centroid on the frame,
            # then update the list of tracked points
            cv2.circle(frame, (int(x), int(y)), int(radius), (0, 255, 255), 2)
            cv2.circle(frame, center, 5, (0, 0, 255), -1)
            print(x, y)

    # update the points queue
    pts.appendleft(center)

    if Y<Y_Uth:
        flag = 1

    if Y>Y_Uth and Y<Y_Lth and flag>0:
        flag =flag +1

    if Y>Y_Lth and flag >=3:
        count = count +1
        GPIO.output(25, GPIO.HIGH)
        time.sleep(.0001)
        GPIO.output(25, GPIO.LOW)
        flag = 0

    print("\ncount =", count)
    cv2.putText(frame, "Count = "+ str(count), (400,80) ,cv2.QT_FONT_NORMAL,1,(0,0,255))
    # show the frame to our screen
    cv2.line(frame,(0,Y_Uth),(resolution[0],Y_Uth),(0,0,0))
    #cv2.line(frame, (0, Y_Mth), (640, Y_Mth), (0, 0, 0))
    cv2.line(frame, (0, Y_Lth), (resolution[0], Y_Lth), (0, 0, 0))
    cv2.imshow("Frame", frame)

    key = cv2.waitKey(1)


    if key & 0xFF == ord('q'):
        break

    fps.update()
vs.stop()
cv2.destroyWindow("img")
cv2.destroyAllWindows()
