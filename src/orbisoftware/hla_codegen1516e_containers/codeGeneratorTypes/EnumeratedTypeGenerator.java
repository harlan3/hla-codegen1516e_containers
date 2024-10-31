/*
 *  HLA Codegen 1516E Containers
 *
 *  Copyright (C) 2024 Harlan Murphy
 *  Orbis Software - orbisoftware@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package orbisoftware.hla_codegen1516e_containers.codeGeneratorTypes;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import orbisoftware.hla_codegen1516e_containers.Utilities;
import orbisoftware.hla_codegen1516e_containers.javaCodeGenerator.LedgerEntry;
import orbisoftware.hla_codegen1516e_containers.javaCodeGenerator.SharedResources.ElementType;
import orbisoftware.hla_pathbuilder.DatabaseAPI;
import orbisoftware.hla_pathbuilder.Utils;
import orbisoftware.hla_pathbuilder.db_classes.DbEnumeratedDatatype;
import orbisoftware.hla_pathbuilder.db_classes.DbEnumeratorDatatype;

public class EnumeratedTypeGenerator {

	public static int indentSpace;

	private Utils utils = new Utils();

	private LedgerEntry ledgerEntry;

	private String indentFormat = "";

	public void setDefaults() {

		indentSpace = 0;
	}

	private void depthIncSpace() {
		indentSpace = indentSpace + 3;

		if (indentSpace > 0)
			indentFormat = String.format("%" + (indentSpace) + "s", "");
	}

	private void depthCurSpace() {

		if (indentSpace > 0)
			indentFormat = String.format("%" + (indentSpace) + "s", "");
	}

	private void depthDecSpace() {
		indentSpace = indentSpace - 3;

		if (indentSpace < 1)
			indentSpace = 0;

		if (indentSpace > 0)
			indentFormat = String.format("%" + (indentSpace) + "s", "");
	}

	public EnumeratedTypeGenerator() {
		
	}
	
	public void generateEnums() {

		try {
			
			final String enumsString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator + "Enums";
			File enumsDir = new File(System.getProperty("user.dir") + File.separator + enumsString);
			
			// Select all enumerated data types
	    	DatabaseAPI databaseAPI = new DatabaseAPI();
	    	String enumeratedSelect = "SELECT * FROM EnumeratedDatatype";
	    	
	    	List<DbEnumeratedDatatype> list1 = databaseAPI.selectFromEnumeratedDatatypeTable(enumeratedSelect);
	    	ArrayList<String> enumeratorList = new ArrayList<String>();

			for (DbEnumeratedDatatype var1 : list1) {

				PrintStream outputStream =
						new PrintStream(new File(enumsDir + File.separator + var1.name + ".java"));
				PrintStream console = System.out;
				System.setOut(outputStream);

				System.out.println("package " + Utilities.packageRoot + "Enums;");
	    		System.out.println();
	    		
				//System.out.println("import orbisoftware.hla_codegen1516e.*;");
				//System.out.println();
				
				System.out.println("public class " + var1.name + " {");
				System.out.println();

				System.out.println("   // Fields");
				String internalValue = utils.getPrimitiveFromEncodingType(utils.convertFromRPRType(var1.type));
				System.out.println("   public " + internalValue + " value = 0;");
				System.out.println();
				
				System.out.println("   // Constructor");
				System.out.println("   public " + var1.name + "() {");
				System.out.println();
				System.out.println("   }");
				
				depthCurSpace();
				
				System.out.println("}");

				System.setOut(console);
				
				setDefaults();
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
