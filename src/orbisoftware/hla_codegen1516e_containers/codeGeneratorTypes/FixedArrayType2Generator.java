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

//This array is of non primitive type
public class FixedArrayType2Generator {

	public static int indentSpace;

	private Utils utils = new Utils();

	private LedgerEntry ledgerEntry;

	private String indentFormat = "";
	
	private List<String> nonBasicFields = new ArrayList<String>();
	
	private int largestStructureMember;
	
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

	public FixedArrayType2Generator(LedgerEntry ledgerEntry) {

		this.ledgerEntry = ledgerEntry;
		nonBasicFields.clear();
		largestStructureMember = 0;
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

		String elementReference = "";
		
		if (elementType == ElementType.Object) {
			System.out.println("package " + Utilities.packageRoot + "Objects." + elementClassname + ".FixedArrays;");
			elementReference = "Objects";
		} else {
			System.out.println("package " + Utilities.packageRoot + "Interactions." + elementClassname + ".FixedArrays;");
			elementReference = "Interactions";
		}

		System.out.println();

		CodeGeneratorJava.printCommonImports(elementReference, elementClassname);
		
		System.out.println("@SuppressWarnings(\"unused\")");
		System.out.println("public class " + ledgerEntry.entryType + " {");
		
		depthIncSpace();
		
		String nativeClass = ledgerEntry.entryClassType;
		
		System.out.println();
		System.out.println(indentFormat + "public " + nativeClass + "[] value = new " + 
				nativeClass + "[" + ledgerEntry.entryCardinality + "];");
		System.out.println();
		System.out.println(indentFormat + "// Constructor");
		System.out.println(indentFormat + "public " + ledgerEntry.entryType + "()" + " {");
		System.out.println();
		
		depthIncSpace();
		
		System.out.println(indentFormat + "for (int i=0; i < " + ledgerEntry.entryCardinality + "; i++)");
		
		depthIncSpace();
		
		System.out.println(indentFormat + "value[i] = new " + nativeClass + "();");
		
		depthDecSpace();
		depthDecSpace();
		
		System.out.println(indentFormat + "}");
		System.out.println("}");
	}
}