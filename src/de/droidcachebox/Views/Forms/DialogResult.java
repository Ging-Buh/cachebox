package de.droidcachebox.Views.Forms;

public enum DialogResult 
{
	Abort, 	//	Der Rückgabewert des Dialogfelds ist Abort (üblicherweise von der Schaltfläche Abbrechen gesendet). 
	Cancel, //	Der Rückgabewert des Dialogfelds ist Cancel (üblicherweise von der Schaltfläche Abbrechen gesendet). 
	Ignore, //	Der Rückgabewert des Dialogfelds ist Ignore (üblicherweise von der Schaltfläche Ignorieren gesendet). 
	No, 	//	Der Rückgabewert des Dialogfelds ist No (üblicherweise von der Schaltfläche Nein gesendet). 
	None, 	//	Nothing wird vom Dialogfeld zurückgegeben. Dies bedeutet, dass das modale Dialogfeld weiterhin ausgeführt wird. 
	OK, 	//	Der Rückgabewert des Dialogfelds ist OK (üblicherweise von der Schaltfläche OK gesendet). 
	Retry, 	//	Der Rückgabewert des Dialogfelds ist Retry (üblicherweise von der Schaltfläche Wiederholen gesendet). 
	Yes, 	//	Der Rückgabewert des Dialogfelds ist Yes (üblicherweise von der Schaltfläche Ja gesendet). 
}
