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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biojava.bio.structure.io.mmcif.ChemCompGroupFactory;
import org.biojava.bio.structure.io.mmcif.model.ChemComp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Generic Implementation of a Group interface.
 * AminoAcidImpl and NucleotideImpl are closely related classes.
 * @see AminoAcidImpl
 * @see NucleotideImpl
 * @author Andreas Prlic
 * @author Horvath Tamas
 * @version %I% %G%
 * @since 1.4
 */
public class HetatomImpl implements Group,Serializable {
	
	private static final Logger logger = LoggerFactory.getLogger(HetatomImpl.class);

	private static final long serialVersionUID = 4491470432023820382L;

	/** this is a "hetatm".
	 *
	 */
	public static final String type = GroupType.HETATM ;

	private static final List<String> WATERNAMES = Arrays.asList(new String[]{"HOH", "DOD",  "WAT"});

	private Map<String, Object> properties ;

	private long id;

	/** stores if 3d coordinates are available. */
	protected boolean pdb_flag ;

	/** 3 letter name of amino acid in pdb file. */
	protected String pdb_name ;

	protected ResidueNumber residueNumber;

	protected List<Atom> atoms ;

	private Chain parent;

	private Map<String,Atom> atomNameLookup;

	private ChemComp chemComp ;

	private List<Group> altLocs;

	/**
	 *  Construct a Hetatom instance. 
	 */
	public HetatomImpl() {
		super();

		pdb_flag = false;
		pdb_name = null ;

		residueNumber = null;
		atoms    = new ArrayList<Atom>();
		properties = new HashMap<String,Object>();
		parent = null;
		chemComp = null;
		altLocs = null;
		
		atomNameLookup = new HashMap<String,Atom>();
	}


	/**
	 *  returns true or false, depending if this group has 3D coordinates or not.
	 * @return true if Group has 3D coordinates
	 */
	public boolean has3D() {
		return pdb_flag;
	}

	/** flag if group has 3D data.
	 *
	 * @param flag  true to set flag that this Group has 3D coordinates
	 */
	public void setPDBFlag(boolean flag){
		pdb_flag = flag ;
	}

	/** Set three character name of Group .
	 *
	 * @param s  a String specifying the PDBName value
	 * @see #getPDBName
	 */
	public void setPDBName(String s) {
		// hetatoms can have pdb_name length < 3. e.g. CU (see 1a4a position 1200 )
		//if (s.length() != 3) {
		//throw new PDBParseException("amino acid name is not of length 3!");
		//}
		if (s != null && s.equals("?")) logger.info("invalid pdbname: ?");
		pdb_name =s ;
		
	}

	/**
	 * Returns the PDBName.
	 *
	 * @return a String representing the PDBName value
	 * @see #setPDBName
	 */
	public String getPDBName() { return pdb_name;}

	/**
	 * {@inheritDoc} 
	 */
	public void addAtom(Atom atom){
		atom.setGroup(this);
		atoms.add(atom);
		if (atom.getCoords() != null){
			// we have got coordinates!
			setPDBFlag(true);
		}
		Atom existingAtom = atomNameLookup.put(atom.getName(),atom);
		
		// if an atom with same name is added to the group that has to be some kind of problem, 
		// we need to warn properly
		if (existingAtom!=null) {
			String altLocStr = "";
			char altLoc = atom.getAltLoc();
			if (altLoc!=' ') altLocStr = "(alt loc '"+altLoc+"')";
			logger.warn("An atom with name "+atom.getName()+" "+altLocStr+" is already present in group: "+this.toString()+". The atom with serial "+atom.getPDBserial()+" will be ignored in look-ups."); 
		}
	};


	/** remove all atoms
	 *
	 */
	public void clearAtoms() {
		atoms.clear();
		setPDBFlag(false);
		atomNameLookup.clear();
	}

	/** 
	 * {@inheritDoc}
	 */
	public int size(){ return atoms.size();   }

	/** 
	 * {@inheritDoc}
	 */
	public List<Atom> getAtoms(){
		return atoms ;
	}

	/** 
	 * {@inheritDoc}
	 */
	public void setAtoms(List<Atom> atoms) {
		
		// important we are resetting atoms to a new list, we need to reset the lookup too!
		atomNameLookup.clear();
		
		for (Atom a: atoms){
			a.setGroup(this);
			atomNameLookup.put(a.getName(),a);
		}
		this.atoms = atoms;
		if ( atoms.size() > 0) {
			pdb_flag = true;
		}

	}

	/**  
	 * {@inheritDoc}
	 */
	public Atom getAtom(String name) {		
		return atomNameLookup.get(name);
	}

	/** 
	 * {@inheritDoc}	
	 */
	public Atom getAtom(int position) {			
			
		if ((position < 0)|| ( position >= atoms.size())) {
			//throw new StructureException("No atom found at position "+position);
			return null;
		}
		Atom a = atoms.get(position);
		return a ;
	}

	/**
	 * {@inheritDoc} 
	 */
	public boolean hasAtom(String fullName) {

		Atom a = atomNameLookup.get(fullName.trim());
		if ( a != null)
			return true;

		return false;

	}

	/**
	 * {@inheritDoc}	 
	 */
	public String getType(){ return type;}

	public String toString(){

		String str = "Hetatom "+ residueNumber + " " + pdb_name +  " "+ pdb_flag;
		if (pdb_flag) {
			str = str + " atoms: "+atoms.size();
		}
		if ( altLocs != null)
			str += " has altLocs :" + altLocs.size(); 


		return str ;

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasAminoAtoms(){
		// if this method call is performed too often, it should become a
		// private method and provide a flag for Group object ...

		if (	hasAtom(StructureTools.CA_ATOM_NAME) && 
				hasAtom(StructureTools.C_ATOM_NAME) && 
				hasAtom(StructureTools.N_ATOM_NAME) && 
				hasAtom(StructureTools.O_ATOM_NAME)) {
			
			// this is the minimun requirement for something to be considered an aminoacid with a backbone
			// note that if the backbone is incomplete it won't be considered an aminoacid
			// if it's a HETATOM that has all these atoms, this is a very good guess that this is 
			// some kind of non-standard aminoacid
			
			return true;
		}
		
		return false;
	}


	/** 
	 * {@inheritDoc}
	 */
	public void setProperties(Map<String,Object> props) {
		properties =  props ;
	}

	/** return properties.
	 *
	 * @return a HashMap object representing the properties value
	 * @see #setProperties
	 */
	public Map<String, Object> getProperties() {
		return properties ;
	}

	/** set a single property .
	 *
	 * @see #getProperties
	 * @see #getProperty
	 */
	public void setProperty(String key, Object value){
		properties.put(key,value);
	}

	/** get a single property .
	 * @param key  a String
	 * @return an Object
	 * @see #setProperty
	 * @see #setProperties
	 */
	public Object getProperty(String key){
		return properties.get(key);
	}


	/** return an AtomIterator.
	 *
	 * @return an Iterator object
	 */
	public Iterator<Atom> iterator() {
		Iterator<Atom> iter = new AtomIterator(this);
		return iter ;
	}

	/** returns and identical copy of this Group object .
	 * @return  and identical copy of this Group object
	 */
	public Object clone(){

		HetatomImpl n = new HetatomImpl();
		n.setPDBFlag(has3D());
		n.setResidueNumber(residueNumber);
		
		n.setPDBName(getPDBName());
		
		// copy the atoms
		for (int i=0;i<atoms.size();i++){
			Atom atom = (Atom) atoms.get(i).clone();
			n.addAtom(atom);
			atom.setGroup(n);
		}
		
		// TODO alt locs are not cloned! do we need to clone them? - JD 2014-12-17
		
		return n;
	}

	/** the Hibernate database ID
	 *
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/** the Hibernate database ID
	 *
	 * @param id the hibernate id
	 */
	public void setId(long id) {
		this.id = id;
	}

	public ChemComp getChemComp() {
		if  ( chemComp == null ) {
			chemComp = ChemCompGroupFactory.getChemComp(pdb_name);
			if (chemComp == null) logger.info("getChemComp: " + pdb_name);
		}
		return chemComp;
	}

	public void setChemComp(ChemComp cc) {
		chemComp = cc;

	}

	/**
	 * {@inheritDoc}
	 */
	public void setChain(Chain chain) {
		this.parent = chain;
		//TODO: setChain(), getChainId() and ResidueNumber.set/getChainId() are
		//duplicating functionality at present and could give different values.
		if (residueNumber != null) {
			residueNumber.setChainId(chain.getChainID());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public Chain getChain() {
		return parent;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getChainId() {
		if (parent == null) {
			return "";
		}
		return parent.getChainID();
	}

	/**
	 * {@inheritDoc}
	 */
	public ResidueNumber getResidueNumber() {

		return residueNumber;
	}


	public void setResidueNumber(ResidueNumber residueNumber) {
		this.residueNumber = residueNumber;
	}

	public void setResidueNumber(String chainId, Integer resNum, Character iCode) {
		this.residueNumber = new ResidueNumber(chainId, resNum, iCode);
	}

	public boolean hasAltLoc() {
		if ( altLocs == null)
			return false;
		if ( altLocs.size() > 0)
			return true;
		return false;
	}

	public List<Group> getAltLocs() {
		if ( altLocs == null)
			return new ArrayList<Group>();
		return altLocs;
	}

	public Group getAltLocGroup(Character altLoc) {
		
			Atom a = getAtom(0);
			if ( a == null) {
				return null;
			}
		
			// maybe the alt loc group in question is myself
			if (a.getAltLoc().equals(altLoc)) {
				return this;
			}

			if (altLocs == null || altLocs.size() == 0)
				return null;

			for (Group group : altLocs) {
				if (group.getAtoms().isEmpty())
					continue;

				// determine this group's alt-loc character code by looking
				// at its first atom's alt-loc character
				Atom b = group.getAtom(0);
				if ( b == null)
					continue;
				
				if (b.getAltLoc().equals(altLoc)) {
					return group;
				}
			}

		return null;
	}

	public void addAltLoc(Group group) {
		if ( altLocs == null) {
			altLocs = new ArrayList<Group>();
		}
		altLocs.add(group);

	}

	@Override
	public boolean isWater() {
		return WATERNAMES.contains(pdb_name);
	}

	/** attempts to reduce the memory imprint of this group by trimming 
	 * all internal Collection objects to the required size.
	 * 
	 */
	public void trimToSize(){

		if ( atoms instanceof ArrayList<?>) {
			ArrayList<Atom> myatoms = (ArrayList<Atom>) atoms;
			myatoms.trimToSize();
		}
		if ( altLocs instanceof ArrayList<?>){
			ArrayList<Group> myAltLocs = (ArrayList<Group>) altLocs;
			myAltLocs.trimToSize();
		}
		atomNameLookup = new HashMap<String,Atom>(atomNameLookup);

		if ( hasAltLoc()) {
			for (Group alt : getAltLocs()){
				alt.trimToSize();
			}
		}


	}

}
