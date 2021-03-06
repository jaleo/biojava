/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 05.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.bio.structure;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 *
 *  AminoAcid inherits most from Hetatom.  Adds a few AminoAcid
 *  specific methods.
 * @author Andreas Prlic
 * @author Jules Jacobsen
 * @since 1.4
 * @version %I% %G%
 *
 */
public class   AminoAcidImpl
extends    HetatomImpl
implements AminoAcid, Serializable
{
	
   private static final long serialVersionUID = -6018854413829044230L;

   /** this is an Amino acid. type is "amino". */
	public static final String type = GroupType.AMINOACID;

	/** IUPAC amino acid residue names
	 */
	private Character amino_char ;

	private Map<String,String>   secstruc;

	private String recordType; // allows to distinguish between AAs that have been created from SEQRES records and ATOM records

	/**
	 * inherits most from Hetero and has just a few extensions.
	 */
	public AminoAcidImpl() {
		super();

		amino_char = null;
		secstruc = new HashMap<String,String>();
		recordType = ATOMRECORD;
	}

	public String getType(){ return type;}

	/**
	 * {@inheritDoc} 
	 */
	public void setSecStruc(Map<String,String> secstr) {
		this.secstruc = secstr ;
	}

	/**
	 * {@inheritDoc} 
	 */
	public Map<String,String> getSecStruc(){
		return secstruc ;
	}

	/**
	 * {@inheritDoc} 
	 */
	public Atom getN()    {return getAtom("N");  }

	/** 
	 * {@inheritDoc}
	 */
	public Atom getCA()   {
		// note CA can also be Calcium, but that can't happen in a standard aminoacid, so this should be safe
		return getAtom("CA"); 
	}

	/** 
	 * {@inheritDoc}
	 */
	public Atom getC()    {return getAtom("C");  }

	/** 
	 * {@inheritDoc}
	 */
	public Atom getO()    {return getAtom("O");  }

	/** 
	 * {@inheritDoc}
	 */
	public Atom getCB()   {return getAtom("CB"); }


	/** 
	 * {@inheritDoc}
	 */
	public  Character getAminoType() {
		return amino_char;
	}

	/** 
	 * {@inheritDoc}
	 */
	public void setAminoType(Character aa){
		amino_char  = aa ;
	}

	/** 
	 * {@inheritDoc}
	 */
    public void setRecordType(String recordName) {
        recordType = recordName;
    }

	/** 
	 * {@inheritDoc}
	 */
    public String getRecordType() {
        return recordType;
	}

	/** string representation. */
	public String toString(){

		String str = "AminoAcid "+ recordType + ":"+ pdb_name + " " + amino_char +
		" " + residueNumber +  " "+ pdb_flag + " " + recordType  ;
		if (pdb_flag) {
			str = str + " atoms: "+atoms.size();
		}
		if ( getAltLocs().size()>0 )
			str += " has altLocs :" + getAltLocs().size(); 

		return str ;

	}
	/** set three character name of AminoAcid.
	 *
	 * @param s  a String specifying the PDBName value
	 * @see #getPDBName()
	 */
	public void setPDBName(String s) {
		
		pdb_name =s ;

	}


	/** returns and identical copy of this Group object .
	 * @return  and identical copy of this Group object
	 */
	public Object clone(){
		AminoAcidImpl n = new AminoAcidImpl();
		n.setPDBFlag(has3D());		
		n.setResidueNumber(getResidueNumber());
		
		n.setPDBName(getPDBName());
		
		n.setAminoType(getAminoType());
		n.setRecordType(recordType);

		// copy the atoms
		for (int i=0;i<atoms.size();i++){
			Atom atom = (Atom) atoms.get(i).clone();
			n.addAtom(atom);
			atom.setGroup(n);
		}
		return n;
	}


}
