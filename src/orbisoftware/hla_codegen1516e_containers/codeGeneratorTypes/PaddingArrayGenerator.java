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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import orbisoftware.hla_codegen1516e_containers.Utilities;
import orbisoftware.hla_codegen1516e_containers.javaCodeGenerator.LedgerEntry;
import orbisoftware.hla_codegen1516e_containers.javaCodeGenerator.SharedResources.ElementType;
import orbisoftware.hla_pathbuilder.Utils;

// This array is of primitive type
public class PaddingArrayGenerator {

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

	public PaddingArrayGenerator(LedgerEntry ledgerEntry) {

		this.ledgerEntry = ledgerEntry;
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

	public void generateClass(String elementClassname, ElementType elementType, LedgerEntry value) {
		
		String primitiveClass;
		
		System.out.println("package " + Utilities.packageRoot + "Misc;");	
		System.out.println();
		
		System.out.println("@SuppressWarnings(\"unused\")");
		System.out.println("public class " + ledgerEntry.entryType + " {");
		System.out.println();
		depthIncSpace();
		
		primitiveClass = utils.getPrimitiveFromClass(ledgerEntry.entryClassType);
		
		System.out.println(indentFormat + "// Fields");
		System.out.println(indentFormat + "public " + primitiveClass + "[] value = new " + primitiveClass + "[1];");
		System.out.println();
		

		System.out.println(indentFormat + "// Constructor");
		System.out.println(indentFormat + "public " + ledgerEntry.entryType + "()" + " {");
		System.out.println();
		
		System.out.println(indentFormat + "}");
		System.out.println("}");
	}

	public void generateClass5(LedgerEntry ledgerEntry) {

		String primitiveClass;
		
		depthCurSpace();
		
		System.out.println();
		
		if (utils.isPrimitiveClass(ledgerEntry.entryClassType))
			primitiveClass = ledgerEntry.entryClassType;
		else
			primitiveClass = utils.getClassFromEncodingType(utils.getEncodingType(ledgerEntry.entryClassType));
		
		String variableName = utils.getDataValueFromPrimitiveClass(ledgerEntry.entryClassType);
		
		System.out.println(indentFormat + "// Class " + primitiveClass);
		System.out.println(indentFormat + "private ArrayList<" + primitiveClass +
				"> internalClassRepresentation = new ArrayList<" + primitiveClass + ">();");
		System.out.println();
		
		System.out.println(indentFormat + "// Setter");
		System.out.println(indentFormat + "public void set" + primitiveClass + "(int index, " +
				primitiveClass + " " + variableName + ") {");
		System.out.println();
		
		depthIncSpace();
		
		System.out.println(indentFormat + "internalClassRepresentation.add(index, " + variableName + ");");
		
		depthDecSpace();
		System.out.println(indentFormat + "}");
		
		System.out.println();
		System.out.println(indentFormat + "// Getter");
		System.out.println(indentFormat + "public " + primitiveClass + " get" + primitiveClass + "(int index) {");
		depthIncSpace();
		System.out.println();
		System.out.println(indentFormat + "return internalClassRepresentation.get(index);");
		depthDecSpace();
		System.out.println(indentFormat + "}");
		System.out.println();
	}
	
	public void generateClass1(LedgerEntry ledgerEntry) {
		
		String primitiveClass;
		
		if (utils.isPrimitiveClass(ledgerEntry.entryClassType))
			primitiveClass = ledgerEntry.entryClassType;
		else
			primitiveClass = utils.getClassFromEncodingType(utils.getEncodingType(ledgerEntry.entryClassType));
		
		depthCurSpace();
		System.out.println(indentFormat + "// Encode outgoing data obtained from internal class representation into DynamicBuffer");
		System.out.println(indentFormat + "public void encode(DynamicBuffer buffer, int alignment) {");
		System.out.println();
		
		depthIncSpace();
		System.out.println(indentFormat + "for (int i=0; i < internalClassRepresentation.size(); i++) {");
		System.out.println();
		depthIncSpace();
		System.out.println(indentFormat + "buffer.put(utilities.getBytesFrom" + primitiveClass + "(internalClassRepresentation.get(i)));");
		depthDecSpace();
		System.out.println(indentFormat + "}");
		depthDecSpace();
		System.out.println(indentFormat + "}");
		System.out.println();
	}
	
	public void processDecodeNode(LedgerEntry ledgerEntry) {
		
		String primitiveClass;
		
		if (utils.isPrimitiveClass(ledgerEntry.entryClassType))
			primitiveClass = ledgerEntry.entryClassType;
		else
			primitiveClass = utils.getClassFromEncodingType(utils.getEncodingType(ledgerEntry.entryClassType));
		
		int primitiveClassSize = utils.getNumberBytesFromPrimitiveType(utils.convertToCamelCase(primitiveClass));
				
		depthCurSpace();
		System.out.println(indentFormat + "// Decode incoming data obtained from DynamicBuffer into internal class representation");
		System.out.println(indentFormat + "public void decode(DynamicBuffer buffer, int alignment) {");
		System.out.println();
		
		depthIncSpace();

		System.out.println(indentFormat + "int elementSize = " + primitiveClassSize + ";");
		System.out.println(indentFormat + "byte[] elementBytes = new byte[elementSize];");
		System.out.println(indentFormat + "int numElements = buffer.getWrittenBytes().length / elementSize;");
		System.out.println();
		System.out.println(indentFormat + "for (int i=0; i < numElements; i++) {");
		System.out.println();
		depthIncSpace();
		System.out.println(indentFormat + "buffer.position(i * elementSize);");
		System.out.println(indentFormat + "buffer.get(elementBytes);");
		System.out.println(indentFormat + "internalClassRepresentation.add(utilities.get" + primitiveClass + "FromBytes(elementBytes));");
		depthDecSpace();
		System.out.println(indentFormat + "}");
		depthDecSpace();
		System.out.println(indentFormat + "}");
		System.out.println();
	}
}