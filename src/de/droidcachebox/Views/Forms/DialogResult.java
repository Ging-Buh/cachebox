package de.droidcachebox.Views.Forms;

public enum DialogResult 
{
	Abort, 	//	Der R�ckgabewert des Dialogfelds ist Abort (�blicherweise von der Schaltfl�che Abbrechen gesendet). 
	Cancel, //	Der R�ckgabewert des Dialogfelds ist Cancel (�blicherweise von der Schaltfl�che Abbrechen gesendet). 
	Ignore, //	Der R�ckgabewert des Dialogfelds ist Ignore (�blicherweise von der Schaltfl�che Ignorieren gesendet). 
	No, 	//	Der R�ckgabewert des Dialogfelds ist No (�blicherweise von der Schaltfl�che Nein gesendet). 
	None, 	//	Nothing wird vom Dialogfeld zur�ckgegeben. Dies bedeutet, dass das modale Dialogfeld weiterhin ausgef�hrt wird. 
	OK, 	//	Der R�ckgabewert des Dialogfelds ist OK (�blicherweise von der Schaltfl�che OK gesendet). 
	Retry, 	//	Der R�ckgabewert des Dialogfelds ist Retry (�blicherweise von der Schaltfl�che Wiederholen gesendet). 
	Yes, 	//	Der R�ckgabewert des Dialogfelds ist Yes (�blicherweise von der Schaltfl�che Ja gesendet). 
}
