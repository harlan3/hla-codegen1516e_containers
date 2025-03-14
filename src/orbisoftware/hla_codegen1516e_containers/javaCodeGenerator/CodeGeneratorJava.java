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
import java.nio.file.Files;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import orbisoftware.hla_codegen1516e_containers.Utilities;
import orbisoftware.hla_codegen1516e_containers.javaCodeGenerator.SharedResources.ElementType;
import orbisoftware.hla_pathbuilder.MMNodeTreeRepository;
import orbisoftware.hla_pathbuilder.NodeTree;
import orbisoftware.hla_pathbuilder.Utils;

public class CodeGeneratorJava {

	private static final String code_gen_dir = "codegen_java";

	private int indentSpace;
	private ArrayList<String> attributesList = new ArrayList<>();
	private ArrayList<String> parametersList = new ArrayList<>();
	
	private GenerateElementNonBasics generateElementNonBasics = new GenerateElementNonBasics();
	private Utilities utilities = new Utilities();
	
	int nodeDepth;
	
	private String indentFormat = "";
	
	private void setDefaults() {

		indentSpace = 0;
		nodeDepth = 0;
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
	
	public void createRootDirectories() {

		try {
			File codegenDir = new File(System.getProperty("user.dir") + File.separator + code_gen_dir);
			File codeGenObjDir = new File(
					System.getProperty("user.dir") + File.separator + code_gen_dir + File.separator +
					Utilities.packageRootDir + File.separator + "Objects");
			File codeGenIntDir = new File(
					System.getProperty("user.dir") + File.separator + code_gen_dir + File.separator + 
					Utilities.packageRootDir + File.separator + "Interactions");

			if (Files.exists(codegenDir.toPath()))
				FileUtils.forceDelete(codegenDir);

			FileUtils.forceMkdir(codegenDir);
			FileUtils.forceMkdir(codeGenObjDir);
			FileUtils.forceMkdir(codeGenIntDir);
			
			final String enumsString = code_gen_dir + File.separator + Utilities.packageRootDir + 
					File.separator + "Enums";
			File enumsDir = new File(System.getProperty("user.dir") + File.separator + enumsString);

			if (Files.exists(enumsDir.toPath()))
				FileUtils.forceDelete(enumsDir);

			FileUtils.forceMkdir(enumsDir);

			final String miscString = code_gen_dir + File.separator + Utilities.packageRootDir +
					File.separator + "Misc";
			File miscDir = new File(System.getProperty("user.dir") + File.separator + miscString);

			if (Files.exists(miscDir.toPath()))
				FileUtils.forceDelete(miscDir);

			FileUtils.forceMkdir(miscDir);
			
		} catch (Exception e) {
		}
	}
	
	public void printHeader1(ElementType elementType, String elementName) {

		String elementReference = "";
		
		if (elementType == ElementType.Object) {
			System.out.println("package " + Utilities.packageRoot + "Objects." + elementName + ";");
			elementReference = "Objects";
		} else if (elementType == ElementType.Interaction) {
			System.out.println("package " + Utilities.packageRoot + "Interactions." + elementName + ";");
			elementReference = "Interactions";
		}

		System.out.println();
		System.out.println("import java.util.Iterator;");
		System.out.println("import java.util.Map;");
		System.out.println();
		CodeGeneratorJava.printCommonImports(elementReference, elementName);
	}
	
	public static void printCommonImports(String elementReference, String elementName) {
		
		//System.out.println("import hla.rti1516e.*;");
		//System.out.println("import hla.rti1516e.encoding.*;");
		//System.out.println("");
		
		System.out.println("import " + Utilities.packageRoot + "Enums.*;");
		System.out.println("import " + Utilities.packageRoot + "Misc.*;");
		System.out.println("import " + Utilities.packageRoot + elementReference + "." + elementName + ".FixedArrays.*;");
		System.out.println("import " + Utilities.packageRoot + elementReference + "." + elementName + ".FixedRecords.*;");
		System.out.println("import " + Utilities.packageRoot + elementReference + "." + elementName + ".LengthlessArrays.*;");
		System.out.println("import " + Utilities.packageRoot + elementReference + "." + elementName + ".NullTerminatedArrays.*;");
		System.out.println("import " + Utilities.packageRoot + elementReference + "." + elementName + ".PrefixedStringLength.*;");
		System.out.println("import " + Utilities.packageRoot + elementReference + "." + elementName + ".VariableArrays.*;");
		System.out.println("import " + Utilities.packageRoot + elementReference + "." + elementName + ".VariantRecords.*;");
		System.out.println();
		
		//System.out.println("import orbisoftware.hla_codegen1516e.*;");
		//System.out.println();
	}

	public void parseAttributes(Node node) {

		boolean found = false;

		if (node.getNodeName().equals("attributes")) {
			found = true;
			attributesList.clear();
			String attributeStrings[] = node.getTextContent().split(",");
			if (!attributeStrings[0].equals("")) { // No attributes case
				for (String attributeString : attributeStrings) {
					attributesList.add(attributeString.trim());
				}
			}
		}

		if (!found) {

			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node currentNode = nodeList.item(i);
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
					// calls this method for all the children which is Element
					parseAttributes(currentNode);
				}
			}
		}
	}

	public void parseParameters(Node node) {

		boolean found = false;

		if (node.getNodeName().equals("parameters")) {
			found = true;
			parametersList.clear();
			String parameterStrings[] = node.getTextContent().split(",");
			if (!parameterStrings[0].equals("")) { // No parameters case
				for (String attributeString : parameterStrings) {
					parametersList.add(attributeString.trim());
				}
			}
		}

		if (!found) {

			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node currentNode = nodeList.item(i);
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
					// calls this method for all the children which is Element
					parseParameters(currentNode);
				}
			}
		}
	}
	
	private void populateNonBasicTypeLedger(Node node) {
		
		nodeDepth++;
		
		// Depth 4 are the attributes/parameters
		if (nodeDepth >= 4) {
			
			NamedNodeMap attributes = node.getAttributes();
			String nodeText = attributes.getNamedItem("TEXT").toString().replaceAll("TEXT=\"", "").replaceAll("\"", "");
			Utils utils = new Utils();
			String nodeSplit[] = nodeText.split(" ");
			String dataFieldName = "";
			if (nodeSplit.length > 1) {
				dataFieldName = nodeSplit[1];
				dataFieldName = utils.lowercaseFirstLetter(dataFieldName);
			}
			String primitiveType = utils.getPrimitiveFromEncodingType(nodeSplit[0]);
			
			// This is a non primitive type that needs to be added to the ledger
			if (primitiveType.equals("Unknown")) {
				
				LedgerEntry ledgerEntry = new LedgerEntry();
				
				ledgerEntry.entryID = attributes.getNamedItem("ID").toString().replaceAll("ID=\"", "").replaceAll("\"", "");
				ledgerEntry.entryType = nodeSplit[0];
				ledgerEntry.entryDataField = dataFieldName;
				ledgerEntry.entryTID = attributes.getNamedItem("TID").toString().replaceAll("TID=\"", "").replaceAll("\"", ""); 
				
				if (ledgerEntry.entryTID.equals("Array")) {
					
					ledgerEntry.entryClassType = attributes.getNamedItem("classtype").toString().replaceAll("classtype=\"", "").replaceAll("\"", "");
					ledgerEntry.entryCardinality = attributes.getNamedItem("cardinality").toString().replaceAll("cardinality=\"", "").replaceAll("\"", "");
					ledgerEntry.entryEncoding = attributes.getNamedItem("encoding").toString().replaceAll("encoding=\"", "").replaceAll("\"", "");
				}
				
				NonBasicTypeLedger.getInstance().nonBasicTypeLedger.put(ledgerEntry.entryID, ledgerEntry);
			}
		}
		
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				// calls this method for all the children which is an Element
				populateNonBasicTypeLedger(currentNode);
			}
		}
		
		nodeDepth--;
	}
	
	private void generateClassFields(Node node) {
		
		nodeDepth++;
		
		// Depth 4 are the attributes/parameters
		if (nodeDepth == 4) {
			
			NamedNodeMap attributes = node.getAttributes();
			String nodeText = 
					attributes.getNamedItem("TEXT").toString().replaceAll("TEXT=\"", "").replaceAll("\"", "");
			Utils utils = new Utils();
			String nodeSplit[] = nodeText.split(" ");
			String dataFieldName = "";
			if (nodeSplit.length > 1) {
				dataFieldName = utils.convertToCamelCase(nodeSplit[1]);
			} else {
				int x=0;
				x++;
			}
				
			String primitiveType = utils.getPrimitiveFromEncodingType(nodeSplit[0]);
			
			depthCurSpace();
			
			// Is not a primitive
			if (primitiveType.equals("Unknown") && !dataFieldName.equals("")) {

				System.out.println(indentFormat + "public " + nodeSplit[0] + " " +
						dataFieldName + " = new " + nodeSplit[0] + "();");
								
			} else {
				
				System.out.println(indentFormat + "public " + primitiveType + " " + 
						dataFieldName + " = " + utilities.primitiveAssignment(primitiveType) + ";");
				
			}
		}
		
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				// calls this method for all the children which is Element
				generateClassFields(currentNode);
			}
		}
		
		nodeDepth--;
	}

	public String findClassname(Node node) {

		boolean found = false;
		String classHandle = "";
		
		NamedNodeMap attrList = node.getAttributes();

		for (int i = 0; i < attrList.getLength(); i++) {

			if (attrList.item(i).getNodeName().equals("classHandle")) {

				found = true;
				classHandle = attrList.item(i).getNodeValue();
				return classHandle;
			}
		}

		if (!found) {

			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node currentNode = nodeList.item(i);
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
					// calls this method for all the children which is Element
					if (!found)
						classHandle = findClassname(currentNode);
				}
			}
		}
		return classHandle;
	}

	public Node findElementNode(Node node, String searchID) {

		NamedNodeMap attrList = node.getAttributes();

		String foundID = attrList.getNamedItem("ID").toString().replaceAll("ID=\"", "").replaceAll("\"", "");

		if (foundID != null) {
			if (foundID.equals(searchID)) {
				return node;
			}
		}

		Node foundNode = null;

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				// calls this method for all the children which are an Element
				foundNode = findElementNode(currentNode, searchID);
				if (foundNode != null)
					break;
			}
		}

		return foundNode;
	}

	public void traverseNode(Node node) {

		NamedNodeMap attrList = node.getAttributes();

		depthCurSpace();
		
		System.out.println(indentFormat + "Level = " + indentSpace / 3);

		for (int i = 0; i < attrList.getLength(); i++) {
			System.out.println(indentFormat + attrList.item(i).getNodeName() + " - " + attrList.item(i).getNodeValue());
		}

		System.out.println();

		NodeList nodeList = node.getChildNodes();
		depthIncSpace();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				// calls this method for all the children which is Element
				traverseNode(currentNode);
			}
		}
		depthDecSpace();
	}
	
	public void generateImportDirectories(ElementType elementType, String elementName) {

		try {		
			final String fixedArraysString = "codegen_java" + File.separator + Utilities.packageRootDir +
					File.separator + elementType.toString() +
					"s" + File.separator + elementName + File.separator + "FixedArrays";
			File fixedArraysDir = new File(System.getProperty("user.dir") + File.separator + fixedArraysString);
			FileUtils.forceMkdir(fixedArraysDir);
			
			final String fixedRecordsString = "codegen_java" + File.separator + Utilities.packageRootDir + 
					File.separator + elementType.toString() +
					"s" + File.separator + elementName + File.separator + "FixedRecords";
			File fixedRecordsDir = new File(System.getProperty("user.dir") + File.separator + fixedRecordsString);
			FileUtils.forceMkdir(fixedRecordsDir);
			
			final String lengthlessString = "codegen_java" + File.separator + Utilities.packageRootDir + 
					File.separator + elementType.toString() +
					"s" + File.separator + elementName + File.separator + "LengthlessArrays";
			File lengthlessArraysDir = new File(System.getProperty("user.dir") + File.separator + lengthlessString);
			FileUtils.forceMkdir(lengthlessArraysDir);
			
			final String nullTerminatedString = "codegen_java" + File.separator + Utilities.packageRootDir +
					File.separator + elementType.toString() +
					"s" + File.separator + elementName + File.separator + "NullTerminatedArrays";
			File nullTerminatedDir = new File(System.getProperty("user.dir") + File.separator + nullTerminatedString);
			FileUtils.forceMkdir(nullTerminatedDir);
			
			final String prefixedStringLengthString = "codegen_java" + File.separator + Utilities.packageRootDir +
					File.separator + elementType.toString() +
					"s" + File.separator + elementName + File.separator + "PrefixedStringLength";
			File prefixedStringLengthDir = new File(System.getProperty("user.dir") + File.separator + prefixedStringLengthString);
			FileUtils.forceMkdir(prefixedStringLengthDir);
			
			final String variableArraysString = "codegen_java" + File.separator + Utilities.packageRootDir +
					File.separator + elementType.toString() + 
					"s" + File.separator + elementName + File.separator + "VariableArrays";
			File variableArraysDir = new File(System.getProperty("user.dir") + File.separator + variableArraysString);
			FileUtils.forceMkdir(variableArraysDir);
			
			final String variantRecordsString = "codegen_java" + File.separator + Utilities.packageRootDir +
					File.separator + elementType.toString() +
					"s" + File.separator + elementName + File.separator + "VariantRecords";
			File variantRecordsDir = new File(System.getProperty("user.dir") + File.separator + variantRecordsString);
			FileUtils.forceMkdir(variantRecordsDir);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generateEnumPlaceHolderFile() {

		try {
			final String generatedTypeString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator + "Enums";
			File generatedTypeDir = new File(System.getProperty("user.dir") + File.separator + generatedTypeString);

			PrintStream outputStream = new PrintStream(
					new File(generatedTypeDir + File.separator + "PlaceHolderEnums.java"));
			PrintStream console = System.out;
			System.setOut(outputStream);
			
			System.out.println("package " + Utilities.packageRoot + "Enums;");
			System.out.println();
			System.out.println("public class PlaceHolderEnums {");
			System.out.println();
			System.out.println("   public PlaceHolderEnums() {}");
			System.out.println("");
			System.out.println("}");
			
			System.setOut(console);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generateMiscPlaceHolderFile() {

		try {
			final String generatedTypeString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator + "Misc";
			File generatedTypeDir = new File(System.getProperty("user.dir") + File.separator + generatedTypeString);

			PrintStream outputStream = new PrintStream(
					new File(generatedTypeDir + File.separator + "PlaceHolderMisc.java"));
			PrintStream console = System.out;
			System.setOut(outputStream);
			
			System.out.println("package " + Utilities.packageRoot + "Misc;");
			System.out.println();
			System.out.println("public class PlaceHolderMisc {");
			System.out.println();
			System.out.println("   public PlaceHolderMisc() {}");
			System.out.println("");
			System.out.println("}");
			
			System.setOut(console);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void generatePlaceHolderFiles(ElementType elementType, String elementName, String generatedType) {

		try {
			// Fixed Record
			final String generatedTypeString = "codegen_java" + File.separator + Utilities.packageRootDir + File.separator +
					elementType.toString() + "s" + File.separator + elementName + File.separator + generatedType;
			File generatedTypeDir = new File(System.getProperty("user.dir") + File.separator + generatedTypeString);

			PrintStream outputStream = new PrintStream(
					new File(generatedTypeDir + File.separator + "PlaceHolder" + generatedType + ".java"));
			PrintStream console = System.out;
			System.setOut(outputStream);
			
			System.out.println("package " + Utilities.packageRoot + elementType.toString() + "s." + elementName + "." + generatedType + ";");
			System.out.println();
			System.out.println("public class PlaceHolder" + generatedType + " {");
			System.out.println();
			System.out.println("   public PlaceHolder" + generatedType + "() {}");
			System.out.println("");
			System.out.println("}");
			
			System.setOut(console);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateCode() {

		MMNodeTreeRepository mmNodeTreeRepository = MMNodeTreeRepository.getInstance();

		int objectArraySize = mmNodeTreeRepository.getObjectArraySize();
		int interactionArraySize = mmNodeTreeRepository.getInteractionArraySize();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			
			File codegenDir = new File(System.getProperty("user.dir") + File.separator + code_gen_dir);
			File codeGenObjDir = new File(
					System.getProperty("user.dir") + File.separator + code_gen_dir + File.separator + Utilities.packageRootDir + File.separator + "Objects");
			File codeGenIntDir = new File(
					System.getProperty("user.dir") + File.separator + code_gen_dir + File.separator + Utilities.packageRootDir + File.separator + "Interactions");

			for (int i = 0; i < objectArraySize; i++) {

				Utils utils = new Utils();
				setDefaults();
				
				File targetObjectDir = new File(codeGenObjDir + File.separator + mmNodeTreeRepository.getObjectName(i));
				FileUtils.forceMkdir(targetObjectDir);

				// String element = mmNodeTreeRepository.getObjectName(i);

				PrintStream outputStream = new PrintStream(
						new File(targetObjectDir + File.separator + mmNodeTreeRepository.getObjectName(i) + ".xml"));
				PrintStream console = System.out;
				System.setOut(outputStream);

				NodeTree nodeTree = mmNodeTreeRepository.getNodeTree(mmNodeTreeRepository.getObjectName(i));
				nodeTree.setDefaults();
				nodeTree.traverseTree(nodeTree.root, false); // False specifies xml output instead of mindmap output
				
				int startNodes = nodeTree.getDocMetaDataStartNode();
				int endNodes = nodeTree.getDocMetaDataEndNode();
				int diffNodes = startNodes - endNodes;
				
				//while (diffNodes > 0) {
				//	System.out.println("</node>");
				//	diffNodes--;
				//}
				
				System.out.println("</doc>");

				System.setOut(console);

				// Read in the XML file that was just generated
				DocumentBuilder db4 = dbf.newDocumentBuilder();
				Document doc4 = db4
						.parse(new File(targetObjectDir + File.separator + mmNodeTreeRepository.getObjectName(i) + ".xml"));

				Node node = doc4.getFirstChild();
				
				// Parse the attributes for the object
				parseAttributes(node);
				
				// Start broilerplate code generation
				
				PrintStream outputStream3 = new PrintStream(
						new File(targetObjectDir + File.separator + mmNodeTreeRepository.getObjectName(i) + ".java"));
				PrintStream console3 = System.out;
				System.setOut(outputStream3);
				
				printHeader1(ElementType.Object, mmNodeTreeRepository.getObjectName(i));

				System.out.println("@SuppressWarnings(\"unused\")");
				System.out.println(("public class " + mmNodeTreeRepository.getObjectName(i)) + " {");
				System.out.println();
				
				// Code generation
				depthIncSpace();
				System.out.println("   " + "// Fields");
				generateClassFields(node);
				System.out.println("");
				
				System.out.println("   // Constructor");
				System.out.println("   public " + mmNodeTreeRepository.getObjectName(i) + "() {");
				System.out.println();
				
				depthIncSpace();
				System.out.println("   }");
				System.out.println("}");
				
				// Clear ledger if any entries exist
				NonBasicTypeLedger.getInstance().clearLedger();
				
				setDefaults();
				
				// Populate ledger
				populateNonBasicTypeLedger(node);
				
				// Generate import directories
				generateImportDirectories(ElementType.Object, mmNodeTreeRepository.getObjectName(i));
				
				// Generate placeholder files
				generatePlaceHolderFiles(ElementType.Object, mmNodeTreeRepository.getObjectName(i), "FixedArrays");
				generatePlaceHolderFiles(ElementType.Object, mmNodeTreeRepository.getObjectName(i), "FixedRecords");
				generatePlaceHolderFiles(ElementType.Object, mmNodeTreeRepository.getObjectName(i), "LengthlessArrays");
				generatePlaceHolderFiles(ElementType.Object, mmNodeTreeRepository.getObjectName(i), "NullTerminatedArrays");
				generatePlaceHolderFiles(ElementType.Object, mmNodeTreeRepository.getObjectName(i), "PrefixedStringLength");
				generatePlaceHolderFiles(ElementType.Object, mmNodeTreeRepository.getObjectName(i), "VariableArrays");
				generatePlaceHolderFiles(ElementType.Object, mmNodeTreeRepository.getObjectName(i), "VariantRecords");
				
				node = doc4.getFirstChild();
				generateElementNonBasics.generateClasses(node, ElementType.Object, mmNodeTreeRepository.getObjectName(i));
				//NonBasicTypeLedger.getInstance().displayLedger();
				
				System.setOut(console);
			}

			for (int i = 0; i < interactionArraySize; i++) {

				Utils utils = new Utils();
				setDefaults();
				
				File targetInteractionDir = 
						new File(codeGenIntDir + File.separator + mmNodeTreeRepository.getInteractionName(i));
				FileUtils.forceMkdir(targetInteractionDir);

				// String element = mmNodeTreeRepository.getObjectName(i);

				PrintStream outputStream = new PrintStream(
						new File(targetInteractionDir + File.separator + mmNodeTreeRepository.getInteractionName(i) + ".xml"));
				PrintStream console = System.out;
				System.setOut(outputStream);

				NodeTree nodeTree = mmNodeTreeRepository.getNodeTree(mmNodeTreeRepository.getInteractionName(i));
				nodeTree.setDefaults();
				nodeTree.traverseTree(nodeTree.root, false); // False specifies xml output instead of mindmap output
				System.out.println("</doc>");

				System.setOut(console);

				// Read in the XML file that was just generated
				DocumentBuilder db4 = dbf.newDocumentBuilder();
				Document doc4 = db4.parse(
						new File(targetInteractionDir + File.separator + mmNodeTreeRepository.getInteractionName(i) + ".xml"));

				Node node = doc4.getFirstChild();
				
				// Parse the parameters for the interaction
				parseParameters(node);
				
				// Start broilerplate code generation
				PrintStream outputStream3 = new PrintStream(
						new File(targetInteractionDir + File.separator + mmNodeTreeRepository.getInteractionName(i) + ".java"));
				PrintStream console3 = System.out;
				System.setOut(outputStream3);

				printHeader1(ElementType.Interaction, mmNodeTreeRepository.getInteractionName(i));

				System.out.println("@SuppressWarnings(\"unused\")");
				System.out.println(("public class " + mmNodeTreeRepository.getInteractionName(i)) + " {");
				System.out.println();
				
				// Code generation
				depthIncSpace();
				generateClassFields(node);
				
				System.out.println("}");
				
				// Clear ledger if any entries exist
				NonBasicTypeLedger.getInstance().clearLedger();
				setDefaults();
				
				// Populate ledger
				populateNonBasicTypeLedger(node);
				
				// Generate import directories
				generateImportDirectories(ElementType.Interaction, mmNodeTreeRepository.getInteractionName(i));
				
				// Generate placeholder files
				generatePlaceHolderFiles(ElementType.Interaction, mmNodeTreeRepository.getInteractionName(i), "FixedArrays");
				generatePlaceHolderFiles(ElementType.Interaction, mmNodeTreeRepository.getInteractionName(i), "FixedRecords");
				generatePlaceHolderFiles(ElementType.Interaction, mmNodeTreeRepository.getInteractionName(i), "LengthlessArrays");
				generatePlaceHolderFiles(ElementType.Interaction, mmNodeTreeRepository.getInteractionName(i), "NullTerminatedArrays");
				generatePlaceHolderFiles(ElementType.Interaction, mmNodeTreeRepository.getInteractionName(i), "PrefixedStringLength");
				generatePlaceHolderFiles(ElementType.Interaction, mmNodeTreeRepository.getInteractionName(i), "VariableArrays");
				generatePlaceHolderFiles(ElementType.Interaction, mmNodeTreeRepository.getInteractionName(i), "VariantRecords");
				
				node = doc4.getFirstChild();
				generateElementNonBasics.generateClasses(node, ElementType.Interaction, mmNodeTreeRepository.getInteractionName(i));
				//NonBasicTypeLedger.getInstance().displayLedger();
				
				System.setOut(console3);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
