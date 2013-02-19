import sys, shlex, numpy, time, math, re

f = open ( sys.argv[1] , 'r')
singularLine = f.readline()
singularLine = singularLine.rstrip()
singularValues = shlex.split(singularLine)

entries = int(sys.argv[2])
sigPower = float(sys.argv[3])

newMatrix = ""
lineCount = 0;

originalFile = sys.argv[1]

fileMatch = re.match(r'(.*)\.\d+\.LSA', originalFile)

outFile = fileMatch.group(1)+".LSA_"+str(entries)+"_"+str(sigPower)

line = f.readline()

while line:
	line = line.rstrip()
	values = shlex.split(line)
	outString = ""
	end = min(len(values), entries)
	for i in range(0, end, 1):
		value = float(values[i]) * pow(float(singularValues[i]), sigPower)
		outString += str(i)+" "+str(value)+" "
	newMatrix += outString+"\n"

	line = f.readline()
	lineCount+=1
	if lineCount % 1000 == 0:
		print lineCount

f.close()

nzEntries = lineCount*entries

fout = open(outFile, "w")

fout.write(str(lineCount)+" "+str(entries)+" "+str(nzEntries)+"\n")
fout.write(newMatrix)
