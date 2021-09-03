from scipy.io import arff
import sys
import random
import csv

def makeArff(arr, filename):
    with open('./' + filename + '_result.arff', 'w', newline='') as f:  # make arff file format
        f.write('''@RELATION pressure

@attribute roc numeric
@attribute mcr numeric
@attribute std numeric
@attribute iran numeric
@attribute kurtosis numeric
@attribute rss numeric
@attribute rms numeric
@attribute label {passing, Non}

@data
''')
        for line in arr:
            f.write(str(line[0])+","+ str(line[1])+","+str(line[2]) + "," + str(line[3]) + "," + str(line[4]) + "," + str(line[5])+ "," + str(line[6]) +"," + str(line[7]) +"\n")


passing_list = []
non_list = []
passing_test = []
non_test = []

if __name__ == "__main__":
    filename = sys.argv[1]  # filename
    ratio = (100 - int(sys.argv[2])) / 100  # train set ratio e.g.,) 70 = train set = 70 / test set 30
    with open(filename, 'r',encoding = 'ISO-8859-1') as f:
        reader = csv.reader(f)
        for line in reader:
            if line[7] == "passing":
                passing_list.append(line)
            else:
                non_list.append(line)

        passingRange = len(passing_list) * ratio
        nonRange = len(non_list) * ratio

        for i in range(int(passingRange)) :
            index = random.randrange(int(passingRange))
            passing_test.append(passing_list[index])
            passing_list.pop(index)

        for i in range(int(passingRange)) :
            index = random.randrange(int(nonRange))
            non_test.append(non_list[index])
            non_list.pop(index)

        while(len(passing_list) != len(non_list)):
            index = random.randrange(len(non_list))
            non_list.pop(index)

    makeArff(passing_list,"passing_trainset")
    makeArff(non_list,"non_trainset")
    makeArff(passing_test, "passing_testset")
    makeArff(non_test, "non_testset")
