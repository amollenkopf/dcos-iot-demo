# Windows Users Only

If you use scp from a Windows workstation ...<br>
Windows injects line endings that are incompatible with Linux.<br>
You can fix the files using "dos2unix" on the boot node as follows:
<pre>
$ sudo yum -y install dos2unix
$ dos2unix *.sh
</pre>


**WARNING:**<br>
Windows users be careful about typos.<br>
Mobaxterm sends strange characters when you backspace.<br>
If you make an error during entry; use Ctrl-C and start over.