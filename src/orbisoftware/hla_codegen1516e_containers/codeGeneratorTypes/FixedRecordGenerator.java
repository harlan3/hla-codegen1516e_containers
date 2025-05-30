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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import orbisoftware.hla_codegen1516e_containers.Utilities;
import orbisoftware.hla_codegen1516e_containers.javaCodeGenerator.CodeGeneratorJava;
import orbisoftware.hla_codegen1516e_containers.javaCodeGenerator.LedgerEntry;
import orbisoftware.hla_codegen1516e_containers.javaCodeGenerator.NonBasicTypeLedger;
import orbisoftware.hla_codegen1516e_containers.javaCodeGenerator.SharedResources.ElementType;
import orbisoftware.hla_pathbuilder.Utils;

public class FixedRecordGenerator {

	public static int indentSpace;

	private Utils utils = new Utils();

	private LedgerEntry ledgerEntry;

	private String indentFormat = "";
	
	private List<String> nonBasicFields = new ArrayList<String>();
	
	private Utilities utilities = new Utilities();
	
	public FixedRecordGenerator(LedgerEntry ledgerEntry) {

		this.ledgerEntry = ledgerEntry;
		nonBasicFields.clear();
	}
	
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

	public Node findElementID(Node node, String elementID) {

		// Check if the node is an element node
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			// Get the attributes of the node
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null) {
				// Check if the node has the "id" attribute and if it matches the search id
				Node idAttr = attributes.getNamedItem("ID");
				if (idAttr != null && idAttr.getNodeValue().equals(elementID)) {
					return node; // Matching node found
				}
			}
		}

		// Recursively search the child nodes
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node found = findElementID(children.item(i), elementID); // Recursive call
			if (found != null) {
				return found; // Return the found node if a match is found
			}
		}

		// Return null if no matching node is found in this subtree
		return null;
	}

	public void printHeader(String elementClassName, ElementType elementType) {

		String elementReference = "";
		
		if (elementType == ElementType.Object) {
			System.out.println("package " + Utilities.packageRoot + "Objects." + elementClassName + ".FixedRecords;");
			elementReference = "Objects";
		} else {
			System.out.println("package " + Utilities.packageRoot + "Interactions." + elementClassName + ".FixedRecords;");
			elementReference = "Interactions";
		}

		System.out.println();

		CodeGeneratorJava.printCommonImports(elementReference, elementClassName);
		
		System.out.println("@SuppressWarnings(\"unused\")");
		System.out.println("public class " + ledgerEntry.entryType + " {");
		System.out.println();

		depthIncSpace();
	}

	public void generateClassChildren(Node node, int depth) {

		final int activeDepth = 1; // This is the depth we are working with

		if (depth == activeDepth)
			generateClassFields(node, depth);

		NodeList children = node.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {

			Node childNode = children.item(i);

			generateClassChildren(childNode, depth + 1);

		}
	}

	private void generateClassFields(Node node, int depth) {

		switch (node.getNodeType()) {

		case Node.ELEMENT_NODE:
			LedgerEntry ledgerEntry = new LedgerEntry();

			// Check and print attributes if any
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null && attributes.getLength() > 0) {
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attribute = attributes.item(i);

					switch (attribute.getNodeName()) {

					case "ID":
						ledgerEntry.entryID = attribute.getNodeValue();
						break;

					case "TEXT":
						String text = attribute.getNodeValue();

						String[] parts = text.split(" ");
						if (parts.length > 1) {
							ledgerEntry.entryType = parts[0];
							ledgerEntry.entryDataField = parts[1];
						} else {
							Utils utils = new Utils();
							ledgerEntry.entryType = text;
							ledgerEntry.entryDataField = utils.convertToCamelCase(text);
						}
						break;

					case "TID":
						ledgerEntry.entryTID = attribute.getNodeValue();
						break;

					case "cardinality":
						ledgerEntry.entryCardinality = attribute.getNodeValue();
						break;

					case "classtype":
						ledgerEntry.entryClassType = attribute.getNodeValue();
						break;

					case "encoding":
						ledgerEntry.entryEncoding = attribute.getNodeValue();
						break;

					default:
						break;
					// System.out.println(indent + " Attribute: " + attribute.getNodeName() + " = "
					// + attribute.getNodeValue());
					}
				}
			}
			
			boolean nonBasicType = false;
			
			if (ledgerEntry.entryTID.equals("Basic")) {
				ledgerEntry.entryType = utils.getPrimitiveFromEncodingType(ledgerEntry.entryType);
				nonBasicType = false;
			}
			else {
				NonBasicTypeLedger.getInstance().nonBasicTypeLedger.put(ledgerEntry.entryID, ledgerEntry);
				nonBasicType = true;
			}

			if (nonBasicType) {
				System.out.println(indentFormat + "public " + ledgerEntry.entryType + " " + ledgerEntry.entryDataField + 
						" = new " + ledgerEntry.entryType + "();");
			} else {
				System.out.println(indentFormat + "public " + ledgerEntry.entryType + " " + ledgerEntry.entryDataField + 
						" = " + utilities.primitiveAssignment(ledgerEntry.entryType) + ";");
			}
			
			break;

		default:
			break;
		}
	}
	
	private void printNodeInfo(Node node, int depth) {
		// Create indentation based on depth
		String indent = " ".repeat(depth * 2);

		switch (node.getNodeType()) {

		case Node.ELEMENT_NODE:
			// Element node (tag)
			System.out.println(indent + "Element: " + node.getNodeName());

			// Check and print attributes if any
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null && attributes.getLength() > 0) {
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attribute = attributes.item(i);
					System.out.println(
							indent + "  Attribute: " + attribute.getNodeName() + " = " + attribute.getNodeValue());
				}
			}
			break;

		default:
			// System.out.println(indent + "Other Node Type: " + node.getNodeName());
			break;
		}
	}
}
