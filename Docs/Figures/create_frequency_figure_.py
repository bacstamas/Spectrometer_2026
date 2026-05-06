import numpy as np
import matplotlib.pyplot as plt

c = 299792458
log10_w = [-14,-12,-10,-8,-6,-4,-2,0,2,4,6,8,10]
log10_f = [22,19,17,15,14,10,6,3,0,-3]
texts = ["Cosmic ray", "Gamma ray", "X ray", "Ultraviolet", "Visible", "Infrared", "Microwave", "Radar", "Television", "NMR", "Radio"]
'''w_texts = [9.303050007098589e-15,
			6.620250826051614e-13,
			7.711435859765324e-11,
			2.373810772143387e-7,
			6.964013375165971e-7,
			0.000012619773108733914,
			0.0005676232709389375,
			0.021720842072596645,
			1.0646681442548267,
			22.76753324029921,
			707.4124578683532]
'''
w_texts = [5.8661661280576426e-15,
3.99993997643172e-13,
4.670300603844623e-11,
1.5596890928587799e-7,
5.898172999280312e-7,
0.0000073272358453417314,
0.00031687459493668415,
0.012611457532084326,
0.6436092867051468,
14.963129137932986,
410.40988215908635]

fig = plt.figure(figsize=(12,5.5))
ax1 = fig.add_subplot(111)
ax2 = ax1.twiny()


for i in zip(texts,w_texts):
	s,x = i
	ax1.text(np.log10(x),0.05,s, weight='demibold', rotation=90, fontsize=14)

pcm = ax1.pcolormesh([[]], cmap='nipy_spectral')
cax = ax1.inset_axes([-11, 0.65, 10, 0.2], transform=ax1.transData)
cbar = fig.colorbar(pcm, cax=cax ,orientation='horizontal')
cbar.ax.xaxis.set_ticks([0, 1])
cbar.ax.set_xticklabels([r'$380\cdot 10^{-9}$', r'$780\cdot 10^{-9}$'], fontsize=12)

ax1.text(-11,0.87,'Ultraviolet', ha='center', fontsize=16, weight='demibold')
ax1.text(-6,0.87,'Visible', ha='center', fontsize=16, weight='demibold')
ax1.text(-1,0.87,'Infrared', ha='center', fontsize=16, weight='demibold')
ax1.plot([-11,-11, np.log10(380e-9),np.log10(380e-9)],[0.55, 0.5,0.37,0.32], color='black')
ax1.plot([-1,-1, np.log10(780e-9),np.log10(780e-9)],[0.55, 0.5,0.37,0.32], color='black')

def tick_function(X):
    return [r"$10^{%d}$" %z for z in X]

ax1.get_yaxis().set_visible(False)
ax1.set_xlim([-15,11])
ax1.set_ylim([0,1])
ax1.set_xticks(log10_w)
ax1.set_xticklabels(tick_function(log10_w), fontsize=12)
ax1.set_xlabel(r"Wavelength [m]", fontsize=16)

f_tick_locations = [np.log10(c/(10**a)) for a in log10_f]

ax2.set_xlim(ax1.get_xlim())
ax2.set_xticks(f_tick_locations)
ax2.set_xticklabels(tick_function(log10_f), fontsize=12)
ax2.set_xlabel(r"Frequency [Hz]", fontsize=16)
plt.tight_layout()
plt.savefig("frequency_figure.png", dpi=200)
plt.show()
