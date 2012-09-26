from scipy.sparse import lil_matrix
A = lil_matrix((4,3), dtype='float32')
A[1,0] = 3.0
A[2,2] = 7.0
A[3,1] = -2.0
A
A.todense()
