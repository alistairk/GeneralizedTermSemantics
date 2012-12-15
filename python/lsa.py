# This python script takes a matrix and performs LSA on it. There are two arguments, the first
# is the number of eigenvectors to use, I recommend 500, the second is the matrix. A new matrix
# will be created with the same name, but with LSA.<eigenvectors> appended to its file name

from csc import divisi2
from divisi2 import SparseMatrix
import sys, shlex, numpy, time, math

singularValues = int(sys.argv[1])

f = open ( sys.argv[2] , 'r')
line = f.readline()
line = line.rstrip()
lineSplit = shlex.split(line)
print line
rows = lineSplit[0]
cols = lineSplit[1]
nz = lineSplit[2]

#eigenvalues = min(int(rows), int(cols))

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

U, s, V = matrix.svd(k=singularValues)

print "SVD complete", time.localtime()[3], time.localtime()[4], time.localtime()[5]

outFile = sys.argv[2]+"."+singularValues+".LSA_matrices"

print outFile

print s

fout = open(outFile, "w" )
#newNZ = int(rows) * eigenvalues
#fout.write(rows + " " + str(eigenvalues) + " " + str(newNZ) + "\n")
for i in range(singularValues):
	fout.write(str(s[i]) + " ")
fout.write("\n")
for i in range(int(rows)):
	for j in range(singularValues):
		fout.write(str(U[i,j]) + " ")
	fout.write("\n")
fout.close()

print "finished", time.localtime()[3], time.localtime()[4], time.localtime()[5]
