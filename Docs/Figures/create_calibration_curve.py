import numpy as np
import matplotlib.pyplot as plt
from scipy.interpolate import interp1d


def moving_average(A, window):
	B = A
	left = (window-1)//2
	right = window-1-left
	for i in range(len(A)):
		if i<left:
			B[i] = np.average(A[:i+right:])
		elif len(A)-1<right:
			B[i] = np.average(A[i-left::])
		else:
			B[i] = np.average(A[i-left:i+right:])
	return B

wav = np.array([450,500,550,570,600,650]) #nm
data_0 = np.array([24.0,45.0,59.0,107.0,79.0,168.0])
data = np.array([19.0,20.0,7.0,12.0,11.0,34.0])

f = interp1d(wav, np.log10(data_0/data), kind='linear')
x = np.linspace(min(wav),max(wav), 100)

A1 = f(x)*(1+np.random.normal(0,0.01,100))
A2 = f(x)*(0.805+np.random.normal(0,0.01,100))
A3 = f(x)*(0.598+np.random.normal(0,0.01,100))
A4 = f(x)*(0.407+np.random.normal(0,0.01,100))
A5 = f(x)*(0.199+np.random.normal(0,0.01,100))

A1 = moving_average(A1,20)
A2 = moving_average(A2,20)
A3 = moving_average(A3,20)
A4 = moving_average(A4,20)
A5 = moving_average(A5,20)


fig = plt.figure(figsize=(12,5.5))
ax1 = fig.add_subplot(121)
ax2 = fig.add_subplot(122)

ax1.plot(x,A1,label=r"$10 \%$")
ax1.plot(x,A2,label=r"$8 \%$")
ax1.plot(x,A3,label=r"$6 \%$")
ax1.plot(x,A4,label=r"$4 \%$")
ax1.plot(x,A5,label=r"$2 \%$")
ax1.legend(fontsize=16)
ax1.set_xlabel(r"Wawelength [nm]", fontsize=18)
ax1.set_ylabel(r"Absorbance", fontsize=18)
#ax1.grid()

B = np.zeros((5,6))
As = [A1,A2,A3,A4,A5]
for i in range(5):
	g = interp1d(x, As[i], kind='linear')
	B[i,::] = g(wav)
colors = ['b','g','r','c','m','y']
for i in range(6):
	ax2.plot([10,8,6,4,2],B[::,i],color = colors[i],label=r'$%d nm$' %(wav[i]))
	ax1.plot(np.ones(5)*wav[i], B[::,i], 'x',color = colors[i])
ax2.legend(fontsize=16)
ax2.set_xlabel(r"Concentration [%]", fontsize=18)
ax2.set_ylabel(r"Absorbance", fontsize=18)
#ax2.grid()


ax1.tick_params(axis='both', which='major', labelsize=14)
ax2.tick_params(axis='both', which='major', labelsize=14)


plt.tight_layout()
plt.savefig("calibration_curve.png", dpi=200)
plt.show()
