import os
import numpy as np
import statistics as stat
import csv
import sys

def getAbsDiff(arr):
    np_array = np.array(arr)
    return float(max(np_array)-min(np_array))

def min_max_normalize(lst):
    normalized = []

    for value in lst:
        normalized_num = (value - min(lst)) / (max(lst) - min(lst))
        normalized.append(normalized_num)

    return normalized


window_arr_pressure = []
window_arr_label = []
if __name__=="__main__":
    for root, dirs, files in os.walk("./"):
        for file_name in files:
            if os.path.splitext(file_name)[-1] == '.csv': # Depends on file type
                with open(file_name, 'r',encoding = 'ISO-8859-1') as f:
                    result = []
                    arr_diff = []
                    reader = csv.reader(f)
                    for txt in reader:
                        window_arr_pressure.append(float(txt[0]))
                        window_arr_label.append(txt[1])
                    endNum = len(window_arr_pressure)-40
                    result = min_max_normalize(window_arr_pressure[0:endNum])
                    for idx, line in enumerate(window_arr_pressure):
                        if idx+39 <= len(window_arr_pressure):
                            arr_diff.append(getAbsDiff(window_arr_pressure[idx:idx+39]))
                    with open('./min_max/'+file_name,'w',newline='') as s:
                        for index,line in enumerate(result):
                            s.write(str(line)+","+window_arr_label[index]+","+str(arr_diff[index])+'\n')
                    window_arr_label = []
                    window_arr_pressure = []
