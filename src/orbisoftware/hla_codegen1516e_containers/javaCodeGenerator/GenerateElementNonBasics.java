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

package orbisoftware.hla_codegen1516e_containers.javaCodeGenerator;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;

import org.w3c.dom.Node;

import orbisoftware.hla_codegen1516e_containers.Utilities;
import orbisoftware.hla_codegen1516e_containers.codeGeneratorTypes.*;
import orbisoftware.hla_codegen1516e_containers.javaCodeGenerator.SharedResources.ElementType;
import orbisoftware.hla_pathbuilder.Utils;

public class GenerateElementNonBasics {

	private int indentSpace = 3;
	private String indentFormat = "";
	private Utils utils = new Utils();

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

	public void generateClasses(Node baseNode, ElementType elementType, String elementName) {

		Iterator<LedgerEntry> valueIterator = NonBasicTypeLedger.getInstance().nonBasicTypeLedger.values().iterator();

		// This should continue until every entry in the ledger has been processed
		while (valueIterator.hasNext()) {

			LedgerEntry value = valueIterator.next();

			switch (value.entryTID) {

			case "Array":
				generateArrayClasses(baseNode, elementType, elementName, value);
				break;
				
			case "SimpleDatatype":
				if (value.entryType.equals("HLAASCIIstringImp"))
					implementPrefixedStringLength(baseNode, elementType, elementName, value);
				break;
				
			case "FixedRecord":
				implementFixedRecord(baseNode, elementType, elementName, value);
				break;

			case "VariantRecord":
				implementVariantRecord(baseNode, elementType, elementName, value);
				break;
			}
		}
	}

	private void generateArrayClasses(Node baseNode, ElementType elementType, String elementName, LedgerEntry value) {

		switch (value.entryEncoding) {

		case "HLAfixedArray":
			implementFixedArray(baseNode, elementType, elementName, value);
			break;

		case "HLAvariableArray":
			implementVariableArray(baseNode, elementType, elementName, value);
			break;

		case "RPRlengthlessArray":
			implementLengthlessArray(baseNode, elementType, elementName, value);
			break;

		case "RPRnullTerminatedArray":
			implementNullTerminatedArray(baseNode, elementType, elementName, value);
			break;
		
		case "RPRpaddingTo32Array":
			implementPaddingArray(baseNode, elementType, elementName, value);
			break;
			
		case "RPRpaddingTo64Array":
			implementPaddingArray(baseNode, elementType, elementName, value);
			break;
		}
	}
			
	private void implementFixedRecord(Node baseNode, ElementType elementType, String elementName, LedgerEntry value) {

		try {
			final String fixedRecordsString = "codegen_java" + File.separator + Utilities.packageRootDir +
					File.separator + elementType.toString() + "s" +
					File.separator + elementName + File.separator + "FixedRecords";
			File fixedRecordsDir = new File(System.getProperty("user.dir") + File.separator + fixedRecordsString);

			FixedRecordGenerator fixedRecordGenerator = new FixedRecordGenerator(value);
			
			PrintStream outputStream = new PrintStream(
					new File(fixedRecordsDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			fixedRecordGenerator.setDefaults();
			Node foundNode = fixedRecordGenerator.findElementID(baseNode, value.entryID);
			fixedRecordGenerator.printHeader(elementName, elementType);
			System.out.println("   " + "// Fields");
			fixedRecordGenerator.generateClassChildren(foundNode, 0);
			System.out.println("");
			
			System.out.println("   // Constructor");
			System.out.println("   public " +  value.entryType + "() {");
			System.out.println();
			System.out.println("   }");
			System.out.println("}");
			System.out.println();

			fixedRecordGenerator.setDefaults();
			this.setDefaults();

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}

	private void implementVariantRecord(Node baseNode, ElementType elementType, String elementName, LedgerEntry value) {

		try {
			final String variantRecordsString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator +
					elementType.toString() + "s" +
					File.separator + elementName + File.separator + "VariantRecords";
			File variantRecordsDir = new File(System.getProperty("user.dir") + File.separator + variantRecordsString);
		
			VariantRecordGenerator variantRecordGenerator = new VariantRecordGenerator(value);

			PrintStream outputStream = new PrintStream(
					new File(variantRecordsDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			variantRecordGenerator.setDefaults();
			Node foundNode = variantRecordGenerator.findElementID(baseNode, value.entryID);
	
			variantRecordGenerator.printHeader(elementName, elementType);
			System.out.println("   " + "// Fields");
			variantRecordGenerator.generateClassChildren(foundNode, 0);
			System.out.println("");
			
			System.out.println("   // Constructor");
			System.out.println("   public " +  value.entryType + "() {");
			System.out.println();
			System.out.println("   }");
			System.out.println("}");
			System.out.println();

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
	
	private void implementPrefixedStringLength(Node baseNode, ElementType elementType, String elementName, LedgerEntry value) {
		
		try {
			final String prefixedStringLengthString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator +
					elementType.toString() + "s" + 
					File.separator + elementName + File.separator + "PrefixedStringLength";
			File prefixedStringLengthDir = new File(System.getProperty("user.dir") + File.separator + prefixedStringLengthString);

			PrintStream outputStream = new PrintStream(
					new File(prefixedStringLengthDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);
		
			PrefixedStringLengthGenerator prefixedStringLengthGenerator = new PrefixedStringLengthGenerator(value);
			prefixedStringLengthGenerator.setDefaults();

			prefixedStringLengthGenerator.generateClass(elementName, elementType, value);

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
	
	private void implementVariableArray(Node baseNode, ElementType elementType, String elementName,
			LedgerEntry value) {

		try {
			final String variableArraysString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator +
					elementType.toString() + "s" + 
					File.separator + elementName + File.separator + "VariableArrays";
			File variableArraysDir = new File(System.getProperty("user.dir") + File.separator + variableArraysString);

			PrintStream outputStream = new PrintStream(
					new File(variableArraysDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			String encodingType;
			
			if (utils.isPrimitiveHLAClass(value.entryClassType)) {
				encodingType = utils.getClassFromEncodingType(value.entryClassType);
				value.entryClassType = encodingType;
			} else {
				encodingType = utils.getEncodingType(value.entryClassType);
			}

			if (!encodingType.equals("Unknown")) { // This array is of primitive type
				
				VariableArrayType1Generator variableArrayType1Generator = new VariableArrayType1Generator(value);
				variableArrayType1Generator.setDefaults();

				variableArrayType1Generator.generateClass(elementName, elementType, value);
				
			} else { // This array is of non primitive type
				
				VariableArrayType2Generator variableArrayType2Generator = new VariableArrayType2Generator(value);
				variableArrayType2Generator.setDefaults();

				variableArrayType2Generator.generateClass(elementName, elementType, value);
			}

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
	
	private void implementLengthlessArray(Node baseNode, ElementType elementType, String elementName,
			LedgerEntry value) {

		try {
			final String lengthlessString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator +
					elementType.toString() + "s" + 
					File.separator + elementName + File.separator + "LengthlessArrays";
			File lengthlessArraysDir = new File(System.getProperty("user.dir") + File.separator + lengthlessString);

			PrintStream outputStream = new PrintStream(
					new File(lengthlessArraysDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			String encodingType;
			
			if (utils.isPrimitiveHLAClass(value.entryClassType)) {
				encodingType = utils.getClassFromEncodingType(value.entryClassType);
				value.entryClassType = encodingType;
			} else {
				encodingType = utils.getEncodingType(value.entryClassType);
			}

			if (!encodingType.equals("Unknown")) { // This array is of primitive type
				
				LengthlessType1Generator lengthlessType1Generator = new LengthlessType1Generator(value);
				lengthlessType1Generator.setDefaults();

				lengthlessType1Generator.printHeader(elementName, elementType, value);
				lengthlessType1Generator.generateClass(value);
				
			} else { // This array is of non primitive type
			
				LengthlessType2Generator lengthlessType2Generator = new LengthlessType2Generator(value);
				lengthlessType2Generator.setDefaults();

				lengthlessType2Generator.printHeader(elementName, elementType, value);
				lengthlessType2Generator.generateClass(value);
			}

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
	
	private void implementPaddingArray(Node baseNode, ElementType elementType, String elementName,
			LedgerEntry value) {

		try {
			final String miscString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator + "Misc";
			File miscDir = new File(System.getProperty("user.dir") + File.separator + miscString);

			PrintStream outputStream = new PrintStream(
					new File(miscDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			String encodingType;
			
			if (utils.isPrimitiveHLAClass(value.entryClassType)) {
				encodingType = utils.getClassFromEncodingType(value.entryClassType);
				value.entryClassType = encodingType;
			} else {
				encodingType = utils.getEncodingType(value.entryClassType);
			}

			if (!encodingType.equals("Unknown")) { // This array is of primitive type
				
				PaddingArrayGenerator paddingArrayGenerator = new PaddingArrayGenerator(value);
				paddingArrayGenerator.setDefaults();

				paddingArrayGenerator.generateClass(elementName, elementType, value);
				
			}

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
	
	private void implementFixedArray(Node baseNode, ElementType elementType, String elementName, LedgerEntry value) {

		try {
			final String fixedArraysString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator + 
					elementType.toString() + "s" +
					File.separator + elementName + File.separator + "FixedArrays";
			File fixedArraysDir = new File(System.getProperty("user.dir") + File.separator + fixedArraysString);

			PrintStream outputStream = new PrintStream(
					new File(fixedArraysDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			String encodingType = utils.getClassFromEncodingType(value.entryClassType);

			if (!encodingType.equals("Unknown")) { // This array is of primitive type
				FixedArrayType1Generator fixedArrayType1Generator = new FixedArrayType1Generator(value);
				fixedArrayType1Generator.setDefaults();

				fixedArrayType1Generator.generateClass(elementName, elementType, value);
			} else { // This array is of non primitive type
				FixedArrayType2Generator fixedArrayType2Generator = new FixedArrayType2Generator(value);
				fixedArrayType2Generator.setDefaults();

				fixedArrayType2Generator.generateClass(elementName, elementType, value);
			}

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}

	private void implementNullTerminatedArray(Node baseNode, ElementType elementType, String elementName,
			LedgerEntry value) {

		try {
			final String nullTerminatedString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator + 
					elementType.toString() + "s" +
					File.separator + elementName + File.separator + "NullTerminatedArrays";
			File nullTerminatedDir = new File(System.getProperty("user.dir") + File.separator + nullTerminatedString);

			PrintStream outputStream = new PrintStream(
					new File(nullTerminatedDir + File.separator + value.entryType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);

			NullTerminatedGenerator nullTerminatedGenerator = new NullTerminatedGenerator(value);
			nullTerminatedGenerator.setDefaults();

			nullTerminatedGenerator.generateClass(elementName, elementType, value);

			// Remove entry from ledger after processing
			NonBasicTypeLedger.getInstance().nonBasicTypeLedger.remove(value.entryID);

		} catch (Exception e) {
		}
	}
}
