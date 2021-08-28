import os
import numpy as np
import statistics as stat
import csv
import sys
from scipy.stats import kurtosis

def getZeroCrossingRate(arr):
    np_array = np.array(arr)
    return float("{0:.4f}".format((((np_array[:-1] * np_array[1:]) < 0).sum()) / len(arr)))


def getMeanCrossingRate(arr):
    return getZeroCrossingRate(np.array(arr) - np.mean(arr))


def getRateOfChange(arr,first,last):
    np_array = np.array(arr)
    return abs(np_array[last]-np_array[first])/window_size # passing 이라는 이벤트라는게 발생했다를 캐치하기 위해서
    #return (np_array[1:] / np_array[:-1] - 1).sum()

def getKurtosis(arr):
    np_array = np.array(arr)
    kur = kurtosis(np_array,fisher=True)
    return kur

def getIrange(arr):
    np_array = np.array(arr)
    return abs(np.percentile(np_array,75) - np.percentile(np_array,25))

def power(list):
    return [x**2 for x in list]

def getRootMeanSquare(arr):
    np_array = np.array(arr)
    np_array = power(np_array)
    return np.sqrt(np.sum(np_array)/float(len(np_array)))

def getRootSumSquare(arr):
    np_array = np.array(arr)
    np_array = power(np_array)
    return np.sqrt(np.sum(np_array))

def getLabel(arr):
    np_array = np.array(arr)
    if "Non" not in np_array:
        return LABEL_PASSING
    else:
        return "Non"

window_size = 40
overlap = 5
LABEL_PASSING = "passing"
roc = []
mcr = []
std = []
iran = []
kur = []
rms = []
rss = []
absDiff = []
label = []


if __name__=="__main__":
    for root, dirs, files in os.walk("./"):
        for file_name in files:
            if os.path.splitext(file_name)[-1] == '.csv': # Depends on file type
                with open(file_name, 'r',encoding = 'ISO-8859-1') as f:
                    reader = csv.reader(f)
                    diff = []
                    window_arr_pressure = []
                    window_arr_label = []
                    for txt in reader:
                        #vals = line[:-1].split(",") # 맨 끝의 \n 제외한 것들을 , 기준으로 나눔
                        window_arr_pressure.append(float(txt[0]))
                        diff.append(txt[2])
                        if str(txt[1]) == LABEL_PASSING:
                            window_arr_label.append(LABEL_PASSING)
                        else:
                            window_arr_label.append("Non")
                    for index, line in enumerate(window_arr_pressure):
                        if index+window_size < len(window_arr_pressure):
                            roc.append(float(getRateOfChange(window_arr_pressure,index,index+window_size))) # Rate of change
                            mcr.append(float(getMeanCrossingRate(window_arr_pressure[index:index+window_size]))) # MCR from previous 30 num of data
                            std.append(float(stat.stdev(window_arr_pressure[index:index+window_size]))) # STD from previous 30 num of data
                            iran.append(float(getIrange(window_arr_pressure[index:index+window_size]))) # interquartile range
                            kur.append(float(getKurtosis(window_arr_pressure[index:index+window_size])))
                            rms.append(float(getRootMeanSquare(window_arr_pressure[index:index+window_size])))
                            rss.append(float(getRootSumSquare(window_arr_pressure[index:index+window_size])))
                            absDiff.append(diff[index])
                            label.append(getLabel(window_arr_label[index:index+window_size])) # each label
                    

    #arff file write
    with open('./arff_files/'+'result.arff','w',newline='') as f: # make arff file format
        f.write('''@RELATION pressure
                    
@attribute roc numeric
@attribute mcr numeric
@attribute std numeric
@attribute iran numeric
@attribute kurtosis numeric
@attribute rss numeric
@attribute rms numeric
@attribute absDiff numeric
@attribute label {passing, Non}

@data
''')
        for index, line in enumerate(roc):
            #f.write(str(iran[index])+ "," +label[index]+"\n")
            f.write(str(roc[index])+","+str(mcr[index])+","+str(std[index]) + "," + str(iran[index]) + "," + str(kur[index]) +  "," + str(rss[index]) + "," +str(rms[index]) + "," +str(absDiff[index]) + "," +label[index]+"\n")
