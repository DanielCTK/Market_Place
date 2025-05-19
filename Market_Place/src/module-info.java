module Market_Place {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires java.desktop;
	requires java.xml;
	requires java.naming;
	requires java.management;
	requires java.base;
	requires java.compiler;
	requires java.datatransfer;
	requires java.prefs;
	requires java.logging;
	requires java.instrument;
	requires java.scripting;
	requires java.sql.rowset;
	requires javafx.graphics;
	requires com.jfoenix;
	requires jbcrypt;
	requires javafx.base;

	  opens ui to javafx.fxml;
	  exports ui;
	  opens model to javafx.base;
	  
//	opens application to javafx.graphics, javafx.fxml;
}
