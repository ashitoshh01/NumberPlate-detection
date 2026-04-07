import cv2
import pytesseract
import sys
import os

def extract_plate_number(image_path):
    if not os.path.exists(image_path):
        print("ERROR: Image file not found at path.")
        return
    try:
        img = cv2.imread(image_path)
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        gray = cv2.bilateralFilter(gray, 11, 17, 17)
        binary = cv2.adaptiveThreshold(gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
                                       cv2.THRESH_BINARY, 11, 2)
        custom_config = r'-c tessedit_char_whitelist=ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 --psm 6'
        text = pytesseract.image_to_string(binary, config=custom_config)
        cleaned_text = "".join(text.split()).strip()
        print(cleaned_text)
    except Exception as e:
        print(f"ERROR: An exception occurred in Python script: {e}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        image_file_path = sys.argv[1]
        extract_plate_number(image_file_path)
    else:
        print("ERROR: No image file path provided to the script.")