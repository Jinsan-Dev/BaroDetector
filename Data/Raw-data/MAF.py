import csv
import sys
import numpy as np
import os

def movingAverageFilter(arr):
    np_array = np.array(arr)
    return np.mean(np_array)


if __name__=="__main__":
    window_arr_pressure = []
    window_arr_label = []
    MAF = []
    label = []
    for root, dirs, files in os.walk("./"):
        for file_name in files:
            if os.path.splitext(file_name)[-1] == '.csv': # Depends on file type
                with open(file_name, 'r',encoding = 'ISO-8859-1') as f:
                    reader = csv.reader(f)
                    for txt in reader:
                        if txt[1] == "Barometer":
                            continue
                        window_arr_pressure.append(float(txt[1])) # baro value
                        window_arr_label.append(str(txt[2])) # label'

                    for index, line in enumerate(window_arr_pressure):
                        if 14 < index < len(window_arr_pressure)-15: 
                            MAF.append(float(movingAverageFilter(window_arr_pressure[index-14:index+15]))) # MFA applied data
                            label.append(window_arr_label[index])

                with open('./MAF/' + file_name,'w',newline='') as f:
                    for index, line in enumerate(MAF):
                        f.write(str(MAF[index]) + "," + str(label[index]) +"\n")
                window_arr_label = []
                window_arr_pressure = []
                MAF = []
                label = []
