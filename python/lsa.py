from csc import divisi2
from divisi2 import SparseMatrix
import sys, shlex, numpy, time

eigenvalues = int(sys.argv[1])

f = open ( sys.argv[2] , 'r')
line = f.readline()
line = line.rstrip()
lineSplit = shlex.split(line)
print line
rows = lineSplit[0]
cols = lineSplit[1]
nz = lineSplit[2]

line = f.readline()

rowList = list()
colList = list()
valList = list()

row = 0
while line:
	line = line.rstrip()
	values = shlex.split(line)
	for i in range(0, len(values), 2):
		column = int(values[i])
		value = float(values[i+1])
		rowList.append(row)
		colList.append(column)
		valList.append(value)
	
	line = f.readline()
	row+=1
	if row % 1000 == 0:
		print row, time.localtime()[3], time.localtime()[4], time.localtime()[5]
		#break;

print "Matrix loaded", time.localtime()[3], time.localtime()[4], time.localtime()[5]

matrix = SparseMatrix.from_lists(valList, rowList, colList, int(rows), int(cols))

print "Matrix built", time.localtime()[3], time.localtime()[4], time.localtime()[5]

U, s, V = matrix.svd(k=eigenvalues)

print "SVD complete", time.localtime()[3], time.localtime()[4], time.localtime()[5]

outFile = sys.argv[2]+".LSA." + str(eigenvalues)

print outFile

fout = open(outFile, "w" )
newNZ = int(rows) * eigenvalues
fout.write(rows + " " + str(eigenvalues) + " " + str(newNZ) + "\n")
for i in range(int(rows)):
	for j in range(int(eigenvalues)):
		fout.write(str(j) + " " + str(U[i,j]) + " ")
	fout.write("\n")
fout.close()

print "finished", time.localtime()[3], time.localtime()[4], time.localtime()[5]
