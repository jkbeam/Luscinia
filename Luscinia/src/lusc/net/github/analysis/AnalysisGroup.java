package lusc.net.github.analysis;


import java.util.*;

import javax.swing.*;

import lusc.net.github.Defaults;
import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.SpectrogramSideBar;


/**
 * AnalysisGroup is a large class that is at the core of comparison-based analysis. At its core are
 * various float[][] matrices that contain the output of comparisons between sets of elements,
 * syllables, syllable transitions and songs 
 * @author Rob
 *
 */
public class AnalysisGroup {

	Song[] songs;
	String[] populations, species;
	int[] syllableRepetitions;
	int eleNumber, eleNumberC, syllNumber, transNumber, songNumber, individualNumber;
	int maxEleLength, maxEleCLength, maxSyllLength, maxTransLength, maxSongLength;
	int[][] lookUpEls, lookUpElsC, lookUpSyls, lookUpTrans, lookUpSongs;
	int[][] individualEle, individualSyl, individualTrans, individualSongs;
	//int[] lookUpSongs;
	int[] eleCounts, sylCounts;
	boolean[][] compScheme;
	float[][] scoresEle, scoresEleC, scoresSyll, scoresSyll2, scoreTrans, scoresSong;
	Defaults defaults;
	DataBaseController dbc;
	SpectrogramSideBar ssb=new SpectrogramSideBar(this);
	
	boolean compressElements=true; //THIS DOESNT BELONG HERE
	
	double alignmentCost=0.2;
	double stitchPunishment=150;
	double syllableRepetitionWeighting=0;

	/**
	 * This constructor takes an array of songs, a Defaults objects (to pass to various 
	 * UI components? And a DataBaseController - which allows the object to load the actual
	 * song data from a database. 
	 * @param songs array of {@link lusc.net.github.Song} objects
	 * @param defaults a {@link lusc.net.github.Defaults} object
	 * @param dbc a {@link lusc.net.github.db.DataBaseController} object
	 */
	public AnalysisGroup(Song[] songs, Defaults defaults, DataBaseController dbc){
		this.songs=songs;
		
		calculateSyllableRepetitions();
		
		this.defaults=defaults;
		this.dbc=dbc;
		songNumber=songs.length;
		
	}
	
	/**
	 * This constructor takes an Array of songs and a Defaults object (almost certainly not needed)
	 * @param songs array of {@link lusc.net.github.Song} objects
	 * @param defaults a {@link lusc.net.github.Defaults} object
	 */
	
	public AnalysisGroup(Song[] songs, Defaults defaults){
		this.songs=songs;
		this.defaults=defaults;
		songNumber=songs.length;
	}
	
	/**
	 * This constructor is for more complex comparison designs, where only certain comparisons
	 * are required
	 * @param songs array of {@link lusc.net.github.Song} objects
	 * @param compScheme a boolean[][] that tells the object which pairs of elements to compare
	 * @param defaults a {@link lusc.net.github.Defaults} object
	 * @param dbc a {@link lusc.net.github.db.DataBaseController} object
	 */
	
	public AnalysisGroup(Song[] songs, boolean[][] compScheme, Defaults defaults, DataBaseController dbc){
		this.songs=songs;
		this.compScheme=compScheme;
		this.defaults=defaults;
		this.dbc=dbc;
		songNumber=songs.length;
	}
	
	/**
	 * Gets the array of Songs used for the analysis
	 * @return an array of {@link lusc.net.github.Song} objects
	 */
	
	public Song[] getSongs(){
		return songs;
	}
	
	/**
	 * Gets a particular Song used for the analysis
	 * @param a index of particular Song to get
	 * @return a {@link lusc.net.github.Song} object
	 */
	
	public Song getSong(int a){
		return songs[a];
	}
	
	/**
	 * Gets the comparison scheme used for complex comparisons
	 * @return a boolean[][] indicating which pairs of songs are to be compared
	 */
	
	public boolean[][] getCompScheme(){
		return compScheme;
	}
	
	/**
	 * Gets an array of names for the populations used in the analysis
	 * @return a String[] containing the names of the populations underlying the songs in the
	 * comparison
	 */
	public String[] getPopulations(){
		return populations;
	}
	
	/**
	 * Gets the Defaults object 
	 * @return a {@link lusc.net.github.Defaults} object
	 */
	public Defaults getDefaults(){
		return defaults;
	}
	
	/**
	 * gets the DataBaseController object
	 * @return a {@link lusc.net.github.db.DataBaseController} object
	 */
	public DataBaseController getDBC(){
		return dbc;
	}
	
	/**
	 * gets the SpectrogramSideBar object - an object containing sketches of subsets of elements
	 * or songs in the list of Songs
	 * @return a {@link lusc.net.github.ui.SpecctrogramSideBar} object
	 */
	
	public SpectrogramSideBar getSSB(){
		return ssb;
	}
	
	/**
	 * gets the boolean switch compressElements which says whether raw element comparisons should
	 * be compressed within a phrase
	 * @return a boolean value
	 */
	
	public boolean getCompressElements(){
		return compressElements;
	}
	
	/**
	 * sets the boolean switch compressElements which says whether raw element comparisons should
	 * be compressed within a phrase
	 * @param a boolean value
	 */
	
	public void setCompressElements(boolean a){
		compressElements=a;
	}
	
	/**
	 * gets the maximum length of various units. 
	 * @param a an int index that decides whether the user wants element, compressed element, syllable,
	 * transition or song lengths
	 * @return an int giving the maximum length of the chosen unit
	 */
	
	public int getMaxLength(int a){
		int b=0;
		if (a==0){b=maxEleLength;}
		else if (a==1){b=maxEleCLength;}
		else if (a==2){b=maxSyllLength;}
		else if (a==3){b=maxTransLength;}
		else if (a==4){b=maxSongLength;}
		return b;
	}
	
	/**
	 * gets the Element comparison matrix
	 * @return a float[][] object of the dissimilarity between pairs of {@link lusc.net.github.Element} objects
	 * @see lusc.net.github.Element
	 */
	
	public float[][] getScoresEle(){
		return scoresEle;
	}
	
	/**
	 * gets the Compressed Element comparison matrix
	 * @return a float[][] object of the dissimilarity between pairs of compressed elements
	 * @see lusc.net.github.Element
	 */
	
	public float[][] getScoresEleC(){
		return scoresEleC;
	}
	
	/**
	 * gets the Syllable comparison matrix
	 * @return a float[][] object of the dissimilarity between pairs of Syllables
	 * @see lusc.net.github.Song
	 */
	
	public float[][] getScoresSyll(){
		return scoresSyll;
	}
	
	/**
	 * gets the Transition comparison matrix
	 * @return  a float[][] object of the dissimilarity between pairs of Transitions
	 * @see lusc.net.github.Song
	 */
	
	public float[][] getScoresTrans(){
		return scoreTrans;
	}
	
	/**
	 * gets the Song comparison matrix
	 * @return a float[][] object of the dissimilarity between pairs of Songs
	 * @see lusc.net.github.Song
	 */
	
	public float[][] getScoresSong(){
		return scoresSong;
	}
	
	/**
	 * gets the number of various types of units
	 * @param a an integer index that determines which unit to use: 0=element,
	 * 1=compressed element, 2=transition, 3=song
	 * @return an integer of the number of the selected type of units there are in the 
	 * comparison
	 */
	
	public int getLengths(int a){
		int b=0;
		if (a==0){b=eleNumber;}
		else if (a==1){b=eleNumberC;}
		else if (a==2){b=syllNumber;}
		else if (a==3){b=transNumber;}
		else if (a==4){b=songNumber;}
		return b;
	}
	
	/**
	 * gets the lookUp object for various units
	 * @param a an integer index that determines which unit to use: 0=element,
	 * 1=compressed element, 2=transition, 3=song
	 * @return an integer [][] array for the selected lookUp object
	 */
	
	public int[][] getLookUp(int a){
		int[][] b=null;
		if (a==0){b=lookUpEls;}
		else if (a==1){b=lookUpElsC;}
		else if (a==2){b=lookUpSyls;}
		else if (a==3){b=lookUpTrans;}
		else if (a==4){b=lookUpSongs;}
		return b;
	}
	
	/**
	 * gets the id for the relevant song/individual for varioua units
	 * @param dataType an integer index that determines which unit to use: 0=element,
	 * 1=compressed element, 2=transition, 3=song
	 * @param i the index of the particular element etc to get from the array 
	 * @return an integer [] array containing the song and individual for the selected element
	 */
	
	public int[] getId(int dataType, int i){
		int[] j=new int[2];
		if (dataType==0){
			j[0]=lookUpEls[i][0];
			j[1]=lookUpEls[i][1];
		}
		else if (dataType==1){
			j[0]=lookUpElsC[i][0];
			j[1]=lookUpElsC[i][1];
		}
		else if (dataType==2){
			j[0]=lookUpSyls[i][0];
			j[1]=lookUpSyls[i][1];
		}
		else if (dataType==3){
			j[0]=lookUpTrans[i][0];
			j[1]=lookUpTrans[i][1];
		}
		else{
			j[0]=i;
			j[1]=-1;
		}
		return (j);
	}
	
	/**
	 * gets an int[][] in which the first index lists individuals in the sample, and the second 
	 * lists the units that belong to that individual
	 * @param a an integer index that determines which unit to use: 0=element,
	 * 1=compressed element, 2=transition, 3=song
	 * @return an int[][] containing individual lookups
	 */
	
	public int[][] getIndivData(int a){
		int[][] s=null;
		if (a==0){s=individualEle;}
		else if (a==1){s=individualEle;}
		else if (a==2){s=individualSyl;}
		else if (a==3){s=individualTrans;}
		else if (a==4){s=individualSongs;}
		return s;
	}
	
	/**
	 * gets the alignmentCost parameter for comparisons 
	 * @return a double value of alignmentCost
	 */
	public double getAlignmentCost(){
		return alignmentCost;
	}
	
	/**
	 * sets the alignmentCost parameter for comparisons 
	 * @param a double value for alignmentCost
	 */
	
	public void setAlignmentCost(double a){
		alignmentCost=a;
	}
	
	
	/**
	 * gets the stichPunishment parameter for comparisons 
	 * @return a double value for stitchPunishment
	 */
	public double getStitchPunishment(){
		return stitchPunishment;
	}
	
	/**
	 * sets the stichPunishment parameter for comparisons 
	 * @param a double value for stitchPunishment
	 */
	public void setStitchPunishment(double a){
		stitchPunishment=a;
	}
	
	/**
	 * gets the syllableRepetitionWeighting parameter for comparisons 
	 * @return a double value for syllableRepetitionWeighting
	 */
	public double getSyllableRepetitionWeighting(){
		return syllableRepetitionWeighting;
	}
	
	/**
	 * sets the syllableRepetitionWeighting parameter for comparisons 
	 * @param a double value for syllableRepetitionWeighting
	 */
	public void setSyllableRepetitionWeighting(double a){
		syllableRepetitionWeighting=a;
	}
	
	/**
	 * Sets the various float[][] score matrices at the heart of this class.
	 * @param a an index from 0 for elements to 4 for songs, and 5 for a second syllable matrix
	 * @param b a float[][] array containing dissimilarities between song units
	 */
	public void setScores(int a, float[][] b){
		if (a==0){scoresEle=b;}
		else if (a==1){scoresEleC=b;}
		else if (a==2){scoresSyll=b;}
		else if (a==3){scoreTrans=b;}
		else if (a==4){scoresSong=b;}
		else if (a==5){scoresSyll2=b;}
	}
	
	/**
	 * Gets the various float[][] score matrices at the heart of this class.
	 * @param a an index from 0 for elements to 4 for songs, and 5 for a second syllable matrix
	 * @return a float[][] array containing dissimilarities between song units
	 */
	public float[][] getScores(int a){
		float[][] b=null;
		if (a==0){b=scoresEle;}
		else if (a==1){b=scoresEleC;}
		else if (a==2){b=scoresSyll;}
		else if (a==3){b=scoreTrans;}
		else if (a==4){b=scoresSong;}
		else if (a==5){b=scoresSyll2;}
		return b;
	}
	
	/**
	 * Gets the labels assigned to each unit.
	 * @param a an index from 0 for elements to 4 for songs
	 * @return a double[] array containing labels for that unit
	 */
	public double[] getLabels(int a){
		double[] s=null;
		if (a==0){s=labelElements();}
		else if (a==1){s=labelElementsC();}
		else if (a==2){s=labelSyllables();}
		else if (a==3){s=labelTransitions();}
		else if (a==4){s=labelSongs();}
		return s;
	}
	
	/**
	 * This method copies/clones one of the score arrays into a new float[][] array and
	 * returns it
	 * @param a an index from 0 for elements to 4 for songs
	 * @return a float[][] dissimilarity matrix
	 */
	public float[][] copy(int a){
		int b=0;
		if (a==0){b=eleNumber;}
		else if (a==1){b=eleNumberC;}
		else if (a==2){b=syllNumber;}
		else if (a==3){b=transNumber;}
		else if (a==4){b=songNumber;}
		
		float[][] temp=new float[b][];
		for (int i=0; i<b; i++){
			float[][] c=getScores(a);
			temp[i]=new float[c[i].length];
			System.arraycopy(c[i], 0, temp[i], 0, temp[i].length);
		}
		return temp;
	}
	
	
	//IS THIS ESSENTIAL?
	/**
	 * This method calculates the number of repetitions for each phrase in the sample
	 */
	public void calculateSyllableRepetitions(){
		int n=0;
		for (int i=0; i<songs.length; i++){
			n+=songs[i].getNumPhrases();
		}
		
		syllableRepetitions=new int[n];
		int k=0;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] p=(int[][])songs[i].getPhrase(j);
				syllableRepetitions[k]=p.length;
				k++;
			}
		}
	}
	
	/**
	 * This method turns every element in the sample into a phrase
	 */
	public void makeEveryElementASyllable(){
		for (int i=0; i<songs.length; i++){
			LinkedList<int[][]> phrases=new LinkedList<int[][]>();
			for (int j=0; j<songs[i].getNumElements(); j++){
				int[][] a=new int[1][1];
				a[0][0]=j;
				phrases.add(a);
			}
			songs[i].setPhrases(phrases);
		}
	}
	
	/**
	 * This method selects one exemplar syllable for each phrase, using a dtw comparison
	 * within the phrase
	 * @see lusc.net.github.analysis.SyllableCompressor
	 */
	public void pickJustOneExamplePerPhrase(){
		SyllableCompressor sc=new SyllableCompressor();
		for (int i=0; i<songs.length; i++){
			sc.compressSong2(songs[i]);
			//sc.compressSong3(songs[i]);
			songs[i]=sc.s;
		}
	}
	
	/**
	 * This method re-segments songs into syllables based on a simple time gap threshold
	 * @param thresh a threshold in ms
	 */
	public void segmentSyllableBasedOnThreshold(double thresh){
		for (int i=0; i<songs.length; i++){
			LinkedList<int[][]> phrases=new LinkedList<int[][]>();
			
			int j=0;
			
			while (j<songs[i].getNumElements()){
				int k=j;
				Element ele=(Element)songs[i].getElement(j);
				while ((ele.getTimeAfter()<thresh)&&(ele.getTimeAfter()>-10000)){
					j++;
					ele=(Element)songs[i].getElement(j);
				}
				int[][] a=new int[1][j-k+1];
				for (int l=k; l<=j; l++){
					a[0][l-k]=l;
				}
				phrases.add(a);
				j++;
			}
			
			songs[i].setPhrases(phrases);
		}
	}
	
	/**
	 * This method is the first step for loading the raw song data (audio data) into
	 * the song object. This may be useful (in future) for methods like spectrogram
	 * cross correlation, where new spectrograms are needed. This version of the method
	 * loads raw audio data for all songs in the song object.
	 */
	public void checkAndLoadRawData(){
		for (int i=0; i<songs.length; i++){
			checkAndLoadRawData(i);
		}
	}
	
	/**
	 * This method is the first step for loading the raw song data (audio data) into
	 * the song object. This may be useful (in future) for methods like spectrogram
	 * cross correlation, where new spectrograms are needed
	 * @param i index for the song
	 */
	public void checkAndLoadRawData(int i){
		System.out.println("CHECKING RAW DATA: "+i);
		if (songs[i].getRawData()==null){
			System.out.println("RAW DATA LOADING: "+i);
			loadSongRawData(i);
		}
	}
	
	/**
	 * This method is the second step for loading the raw song data (it is called from
	 * the previous method). If you call this method directly, there is no time-saving
	 * check whether the song is already loaded.
	 * @param i index for the song
	 */
	void loadSongRawData(int i){
		Song song=dbc.loadSongFromDatabase(songs[i].getSongID(), 0);
		songs[i]=song;
		//System.out.println(songs[i].getMaxF()+" "+songs[i].getDynRange());
		if ((songs[i].getMaxF()<=1)||(songs[i].getDynRange()<1)){
			defaults.getSongParameters(songs[i]);
		}
		System.out.println(songs[i].getRawDataLength()+" "+songs[i].getSongID());
		
	}
	
	
	/**
	 * Organizational method that calls a range of methods for calculating names and lookups
	 * for the units in the analysis.
	 */
	public void makeNames(){
		countEleNumber();
		calculateMaxima();
		makeEleNames();
		makeEleNamesC();
		makeSyllNames();
		makeTransNames();
		makeSongNames();
		//labelElements();
		//labelSyllables();
		//labelTransitions();
		//labelSongs();
		getPopulationNames();
	}
	
	
	/**
	 * This method calculates the number of different units and makes the look-up tables
	 */
	public void countEleNumber(){
		eleNumber=0;
		syllNumber=0;
		eleNumberC=0;
		lookUpSongs=new int[songNumber][2];
		sylCounts=new int[songNumber];
		eleCounts=new int[songNumber];
		int songCount=songNumber;
		for (int i=0; i<songs.length; i++){
			//if (songs[i].phrases==null){songs[i].interpretSyllables();}
			lookUpSongs[i][0]=i;
			lookUpSongs[i][1]=eleNumber;
			if (songs[i].getNumPhrases()==0){
				//System.out.println("alert");
				songCount--;
			}
			//System.out.println(i+" "+songs[i].getNumPhrases());
			sylCounts[i]=songs[i].getNumPhrases();
			eleCounts[i]=songs[i].getNumElements();
			eleNumber+=songs[i].getNumElements();

			int a=songs[i].getNumPhrases();
			if (a>0){
				syllNumber+=a;
				for (int j=0; j<a; j++){
					int[][] p=(int[][])songs[i].getPhrase(j);
					eleNumberC+=p[0].length;
				}
			}
		}
		transNumber=syllNumber-songCount;
		//System.out.println(syllNumber+" "+songNumber+" "+songCount);
		if (transNumber<0){
			transNumber=0;
		}
		//transNumber=syllNumber;
		lookUpEls=new int[eleNumber][2];
		lookUpElsC=new int[eleNumberC][3];
		lookUpSyls=new int[syllNumber][2];
		lookUpTrans=new int[transNumber][4];
		
		int count1=0;
		int count2=0;
		int count3=0;
		int count4=0;
		for (int i=0; i<songs.length; i++){
			int a1=songs[i].getNumElements();
			if (a1>0){
				
				for (int j=0; j<a1; j++){
					lookUpEls[count1][0]=i;
					lookUpEls[count1][1]=j;
					count1++;
				}
			}
			int a=songs[i].getNumPhrases();
			if (a>0){
				int c=0;
				for (int j=0; j<a; j++){
					int[][] p=(int[][])songs[i].getPhrase(j);
					for (int k=0; k<p[0].length; k++){
						lookUpElsC[count2][0]=i;
						
						int b=p.length-1;
						while (p[b][k]==-1){b--;}
					
						lookUpElsC[count2][1]=p[b][k];
						lookUpElsC[count2][2]=c;
						count2++;
						c++;
					}				
					lookUpSyls[count3][0]=i;
					lookUpSyls[count3][1]=j;
					
					if (j>0){
						lookUpTrans[count4][0]=i;
						lookUpTrans[count4][1]=j;
						lookUpTrans[count4][2]=count3-1;
						lookUpTrans[count4][3]=count3;
						count4++;
					}
					count3++;
				}
				
				//lookUpTrans[count4][0]=i;
				//lookUpTrans[count4][1]=-1;
				//count4++;
				
			}
		}
	}
	
	/**
	 * This method calculates maximum lengths for some units
	 */
	public void calculateMaxima(){
		maxEleLength=0;
		maxSyllLength=0;
		maxTransLength=0;
		maxSongLength=0;
		int a;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].getNumElements(); j++){
				Element ele=(Element)songs[i].getElement(j);
				a=ele.getLength();
				if (a>maxEleLength){maxEleLength=a;}
			}
			int syllLengthPrev=0;
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][]p=(int[][])songs[i].getPhrase(j);
				boolean problem=false;
				for (int k=0; k<p.length; k++){
					if (p[k].length==0){problem=true;}
				}
				if (problem){
					String s="There seems to be a problem with individual "+songs[i].getIndividualName()+", song "+songs[i].getName()+", syllable "+(j+1);
					JOptionPane.showMessageDialog(null,s,"Alert!", JOptionPane.ERROR_MESSAGE);
				}
				else{
					a=p.length-1;
					while (p[a][p[a].length-1]==-1){a--;}
				
					Element ele1=(Element)songs[i].getElement(p[a][0]);
					Element ele2=(Element)songs[i].getElement(p[a][p[a].length-1]);
				
					int syllLength=ele2.getBeginTime()+ele2.getLength()-ele1.getBeginTime();
				
					if (syllLength>maxSyllLength){maxSyllLength=syllLength;}
					
					int transLength=syllLength+syllLengthPrev;
					if (transLength>maxTransLength){maxTransLength=transLength;}
					
					syllLengthPrev=(int)Math.round(syllLength+(ele2.getTimeAfter()/ele2.getTimeStep()));
					
				}
			}
			Element ele1=(Element)songs[i].getElement(0);
			Element ele2=(Element)songs[i].getElement(songs[i].getNumElements()-1);
			
			int songLength=ele2.getBeginTime()+ele2.getLength()-ele1.getBeginTime();
			if (songLength>maxSongLength){maxSongLength=songLength;}
		}
	}

	/**
	 * Makes and Gets names for different units
	 * @param a an index from 0 for Elements to 4 for Songs
	 * @return
	 */
	public String[] getNames(int a){
		String[] b=null;
		if (a==0){b=makeEleNames();}
		else if (a==1){b=makeEleNamesC();}
		else if (a==2){b=makeSyllNames();}
		else if (a==3){b=makeTransNames();}
		else if (a==4){b=makeSongNames();}
		return b;
	}

	/**
	 * makes names for each Element in the sample
	 * @return a String array of element names
	 * @see lusc.net.github.Element
	 */
	public String[] makeEleNames(){
		String [] eleNames=new String[eleNumber];
		int count=0;
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].getName();
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].getName().substring(0, length-4);
			}
			for (int j=0; j<songs[i].getNumElements(); j++){
				Integer gr=new Integer(j+1);
				eleNames[count]=songs[i].getIndividualName()+":"+n3+","+gr.toString();
				//eleNames[count]=songs[i].getIndividualName();
				count++;
			}
		}
		return eleNames;
	}
	
	/**
	 * makes names for each Compressed Element in the sample
	 * @return a String array of element names
	 * @see lusc.net.github.Element
	 */
	public String[] makeEleNamesC(){
		String[] eleNamesC=new String[eleNumberC];
		int count=0;
		int maxLength=0;
		for (int i=0; i<songs.length; i++){
			if (songs[i].getNumElements()>maxLength){
				maxLength=songs[i].getNumElements();
			}
		}
		String[] numberString=new String[maxLength+1];
		for (int i=0; i<numberString.length; i++){
			Integer gr1=new Integer(i);
			numberString[i]=gr1.toString();
		}
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].getName();
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].getName().substring(0, length-4);
			}
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] p=(int[][])songs[i].getPhrase(j);
				
				for (int k=0; k<p[0].length; k++){
					StringBuffer sb=new StringBuffer();
					
					for (int w=0; w<p.length; w++){
						if (p[w].length>k){
							sb.append(numberString[p[w][k]+1]+",");
						}
					}
					sb.deleteCharAt(sb.length()-1);
					eleNamesC[count]=songs[i].getIndividualName()+": "+n3+": "+sb.toString();
					
					//eleNamesC[count]=songs[i].getIndividualName()+":"+n3+","+numberString[j+1]+"."+numberString[k+1];
					
					count++;
				}
			}
		}
		return eleNamesC;
	}
	
	/**
	 * makes names for each Syllable in the sample
	 * @return a String array of syllable names
	 * @see lusc.net.github.Song
	 */
	public String[] makeSyllNames(){
		String[] syllNames=new String[syllNumber];
		//syllLabels=new double[syllNumber];
		int count=0;
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].getName();
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].getName().substring(0, length-4);
			}
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				Integer gr=new Integer(j+1);
				syllNames[count]=songs[i].getIndividualName()+":"+n3+","+gr.toString();
				
				
				if (syllNames[count].startsWith("ElementOne")){
					syllNames[count]="1";
					//syllLabels[count]=0;
				}
				if (syllNames[count].startsWith("ElementTwo")){
					syllNames[count]="2";
					//syllLabels[count]=0.2;
				}
				if (syllNames[count].startsWith("ElementThree")){
					syllNames[count]="3";
					//syllLabels[count]=0.4;
				}
				if (syllNames[count].startsWith("ElementFour")){
					syllNames[count]="4";
					//syllLabels[count]=0.6;
				}
				if (syllNames[count].startsWith("ElementFive")){
					syllNames[count]="5";
					//syllLabels[count]=0.8;
				}
				if (syllNames[count].startsWith("ElementSix")){
					syllNames[count]="6";
					//syllLabels[count]=1;
				}
				
				count++;
			}
		}
		return syllNames;
	}
	
	/**
	 * makes names for each Transition in the sample
	 * @return a String array of transition names
	 * @see lusc.net.github.Song
	 */
	public String[] makeTransNames(){
		String[] transNames=new String[transNumber];
		
		for (int i=0; i<transNumber; i++){
			String n=songs[lookUpTrans[i][0]].getName();
			if (n.endsWith(".wav")){
				int length=n.length();
				n=n.substring(0, length-4);
			}
			Integer gr=new Integer(lookUpTrans[i][1]+1);
			transNames[i]=songs[lookUpTrans[i][0]].getIndividualName()+":"+n+","+gr.toString();
		}
		return transNames;
	}
	
	/**
	 * makes names for each Song in the sample
	 * @return a String array of song names
	 * @see lusc.net.github.Song
	 */
	public String[] makeSongNames(){
		String[] songNames=new String[songNumber];
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].getName();
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].getName().substring(0, length-4);
			}
			
			//songNames[i]=songs[i].getIndividualName()+":"+n3;
			songNames[i]=n3;
		}
		return songNames;
	}
	
	/**
<<<<<<< HEAD
	 * Labels Elements in the sample by their position in the 
	 * @return
=======
	 * Labels Elements in the sample by their position in the song
	 * @return a double[] containing the position of elements
>>>>>>> FETCH_HEAD
	 */
	public double[] labelElements(){
		double[] eleLabels=new double[eleNumber];
		for (int i=0; i<eleNumber; i++){
			eleLabels[i]=lookUpEls[i][1]/(songs[lookUpEls[i][0]].getNumElements()-1.0);
			//eleLabels[i]=lookUpEls[i][0]/(songNumber-1.0);
		}
		return eleLabels;
	}
	
	/**
	 * Labels Compressed Elements in the sample by their position in the song
	 * @return a double[] containing the position of elements
	 */
	
	public double[] labelElementsC(){
		double[] eleLabels=new double[eleNumberC];
		for (int i=0; i<eleNumberC; i++){
			int j=lookUpElsC[i][0];
			double a=0;
			for (int k=0; k<songs[j].getNumPhrases(); k++){
				int[][]p=(int[][])songs[j].getPhrase(k);
				a+=p[0].length;			
			}
			a--;
			eleLabels[i]=lookUpElsC[i][2]/a;
		}
		return eleLabels;
	}
	
	/**
	  * Sets syllable labels using an input int[] data set
	 * @return a double[] containing syllables labels (normalized by the maximum value of inputs)
	 */
	
	public double[] setSyllLabels(int[] dat){
		double[] syllLabels=new double[syllNumber];
		System.out.println("Setting syll labels 1");
		if (dat.length==syllNumber){
	
			int max=0;
			int min=100000;
			
			for (int i=0; i<syllNumber; i++){
				if (dat[i]>max){
					max=dat[i];
				}
				if ((dat[i]>=0)&&(dat[i]<min)){
					min=dat[i];
				}
			}
			double maxd=max;
			double mind=min;
			for (int i=0; i<syllNumber; i++){
				syllLabels[i]=(float)((dat[i]-mind)/(maxd-mind));
			}
		}
		return syllLabels;
	}
	
	
	/**
	 * Labels syllables, according to position of phrase within song
	 * @return a double[] (range 0-1) giving relative position of each syllable within the song
	 */
	public double[] labelSyllables(){
				
		double[] syllLabels=new double[syllNumber];
		double max=0;
		for (int i=0; i<songs.length; i++){
			if (songs[i].getNumPhrases()>max){max=songs[i].getNumPhrases();}
		}
		for (int i=0; i<syllNumber; i++){
			/*
			int j=lookUpSyls[i][0];
			String s=songs[j].species;
			if (s.startsWith("L")){
				syllLabels[i]=0;
			}
			else if(s.startsWith("H")){
				syllLabels[i]=1;
			}
			else{
				syllLabels[i]=0.5;
			}
			*/
			/*
			int j=lookUpSyls[i][0];
			String s=songs[j].individualName;
			if (s.startsWith("cap")){
				syllLabels[i]=0;
			}
			else if(s.startsWith("vin")){
				syllLabels[i]=1;
			}
			else{
				syllLabels[i]=0.5;
			}
			*/
			//syllLabels[i]=(songs[lookUpSyls[i][0]].getNumPhrases()-lookUpSyls[i][1])/(max);	
			syllLabels[i]=lookUpSyls[i][1]/(songs[lookUpSyls[i][0]].getNumPhrases()-1.0);
			//System.out.println(syllLabels[i]+" "+lookUpSyls[i][1]+" "+songs[lookUpSyls[i][0]].getNumPhrases());
			/*int j=lookUpSyls[i][0];
			if (songs[j].name.startsWith("H")){
				syllLabels[i]=0;
			}
			else if (songs[j].name.startsWith("G")){
				syllLabels[i]=0.5;
			}
			else{
				syllLabels[j]=1;
			}*/
		}
		return syllLabels;
	}
	
	/**
	 * Labels transitions according to the relative position of the transition within the song
	 * @return a double[] (range 0-1) giving relative position of transition within the song
	 */

	public double[] labelTransitions(){
		double[] transLabels=new double[transNumber];
		for (int i=0; i<transNumber; i++){
			transLabels[i]=(lookUpTrans[i][1]-1.0)/(songs[lookUpTrans[i][0]].getNumPhrases()-2.0);
			if (songs[lookUpTrans[i][0]].getNumPhrases()<=2){
				transLabels[i]=0.5;
			}
		}
		return transLabels;
	}
	
	/**
	 * Labels songs according to an arbitrary naming system. Needs to be updated!
	 * @return a double[] of song labels
	 */
	
	public double[] labelSongs(){
		double[] songLabels=new double[songs.length];
		
		for (int i=0; i<songs.length; i++){
		
			String s=songs[i].getIndividualName();
			if (s.startsWith("cap")){
				songLabels[i]=0;
			}
			else if(s.startsWith("vin")){
				songLabels[i]=1;
			}
			else{
				songLabels[i]=0.5;
			}
		}
		return songLabels;
	}
	
	/**
	 * calculates population names - extracting all the population names found in the sample of songs to be analyzed.
	 */
	
	public void getPopulationNames(){
		LinkedList<String> populationName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].getPopulation();
			boolean matched=false;
			for (int j=0; j<populationName.size(); j++){
				String t=populationName.get(j);
				if (t.startsWith(s)){
					matched=true;
					j=populationName.size();
				}
			}
			if (!matched){
				//System.out.println("NEW POPULATION: "+s);
				populationName.add(s);
			}
		}
		populations=new String[populationName.size()];
		
		populations=populationName.toArray(populations);
	}
	
	/**
	 * Calculates species names - looking up all the species in the analyzed sample
	 */
	
	public void getSpeciesNames(){
		LinkedList<String> speciesName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].getSpecies();
			boolean matched=false;
			for (int j=0; j<speciesName.size(); j++){
				String t=speciesName.get(j);
				if (t.startsWith(s)){
					matched=true;
					j=speciesName.size();
				}
			}
			if (!matched){
				//System.out.println("NEW POPULATION: "+s);
				speciesName.add(s);
			}
		}
		species=new String[speciesName.size()];
		
		species=speciesName.toArray(species);
	}
	
	/**
	 * For a given input hierarchical level, and id, returns the population id
	 * @param a input level (from Element to Song)
	 * @param b index to look up
	 * @return an int giving the index of the population id (see getPopulationNames)
	 */
	
	public int lookUpPopulation(int a, int b){
		int c=0;
		if (a==0){
			c=lookUpEls[b][0];
		}
		else if (a==1){
			c=lookUpElsC[b][0];
		}
		else if (a==2){
			c=lookUpSyls[b][0];
		}
		else if (a==3){
			c=lookUpTrans[b][0];
		}
		else{
			c=b;
		}
		int r=0;
		for (int i=0; i<populations.length; i++){
			if (populations[i].startsWith(songs[c].getPopulation())){
				r=i;
				i=populations.length;
			}
		}
		return r;
	}
	
	/**
	 * returns an array containing the species id for each item
	 * @param h hierarchical level (from Element to Song)
	 * @return an int[] containint the species id for each unit with the sample (see getSpeciesNames)
	 */
	
	public int[] getSpeciesListArray(int h){
		getSpeciesNames();
		int[] results=new int[1];
		if (h==0){
			results=new int[lookUpEls.length];
		
			for (int i=0; i<lookUpEls.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpEls[i][0]].getSpecies().equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		if (h==1){
			results=new int[lookUpElsC.length];
		
			for (int i=0; i<lookUpElsC.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpElsC[i][0]].getSpecies().equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		if (h==2){
			results=new int[lookUpSyls.length];
		
			for (int i=0; i<lookUpSyls.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpSyls[i][0]].getSpecies().equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		else if (h==3){
			results=new int[lookUpTrans.length];
			
			for (int i=0; i<lookUpTrans.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpTrans[i][0]].getSpecies().equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		else if (h==4){
			results=new int[songs.length];
			for (int i=0; i<songs.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[i].getSpecies().equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		return results;
	}
	
	/**
	 * calculates and returns an int array for the position of units within the song
	 * This is expressed as an integer from 0 (beginning) to 6 (end) of the song. This is 
	 * chaffinch-specific and should be phased out to something more general. This was used
	 * for some experiments with alternative types of entropy estimation.
	 * @param h hierarchical level (from Element to Syllable)
	 * @return an int[] indicating position.
	 */
		
	public int[] getPositionListArray(int h){
		
		int[] results=new int[1];
		
		if (h==1){

			results=new int[lookUpSyls.length];
		
			for (int i=0; i<lookUpSyls.length; i++){
				
				int j=i;
				while ((j<lookUpSyls.length)&&(lookUpSyls[j][0]==lookUpSyls[i][0])){
					j++;
				}
				j--;
			
				double p=lookUpSyls[i][1]/(lookUpSyls[j][1]+0.0);
			
				results[i]=(int)Math.round(p*6);
			
			}
		}
		else if (h==2){
			results=new int[lookUpTrans.length];
			
			for (int i=0; i<lookUpTrans.length; i++){
				
				int j=i;
				while ((j<lookUpTrans.length)&&(lookUpTrans[j][0]==lookUpTrans[i][0])){
					j++;
				}
				j--;
				
				double p=lookUpTrans[i][1]/(lookUpTrans[j][1]+0.0);
				
				results[i]=(int)Math.round(p*6);
				
			}
		}
		
		return results;
	}
	
	/**
	 * This method identifies which Individual is associated with which song/song unit.
	 * @param type hierarchical level (from Element to Song)
	 * @return an int array showing the individual associated with each unit.
	 */
	
	public int[] calculateSongAssignments(int type){
		int[] results;
		int[][] lookUp=getLookUp(type);
		results=new int[lookUp.length];
		
		for (int i=0; i<lookUp.length; i++){
			for (int j=0; j<individualSongs.length; j++){
				for (int k=0; k<individualSongs[j].length; k++){
					if (individualSongs[j][k]==lookUp[i][0]){
						results[i]=j;
					}
				}
			}
		}		
		return results;
	}
	
	/**
	 * Calculates and returns the population id for each unit.
	 * @param type hierarchical level (from Element to Song)
	 * @return an int[] giving the population id for each unit.
	 */
	
	public int[] getPopulationListArray(int type){
		getPopulationNames();
		int[][] lookUp=getLookUp(type);
		int[] results=new int[lookUp.length];
		
		for (int i=0; i<lookUp.length; i++){
			for (int j=0; j<populations.length; j++){
				if (songs[lookUp[i][0]].getPopulation().equals(populations[j])){
					results[i]=j;
					j=populations.length;
				}
			}
		}
		return results;
	}
	
	/**
	 *This method calculates the class arrays "individuals" which contains the song-types, transitions and elements owned by each individual in the set of songs
	 */
	
	public void calculateIndividuals(){
		int countInds=0;
		int[][] indLocs=new int[songNumber][5];
		for (int i=0; i<songNumber; i++){
			boolean found=false;
			for (int j=0; j<countInds; j++){
				if (songs[i].getIndividualID()==indLocs[j][0]){
					indLocs[j][1]++;
					indLocs[j][2]+=songs[i].getNumElements();
					indLocs[j][3]+=songs[i].getNumPhrases();
					indLocs[j][4]+=songs[i].getNumPhrases()-1;
					found=true; 
					j=countInds;
				}
			}
			if(!found){
				indLocs[countInds][0]=songs[i].getIndividualID();
				System.out.println((countInds+1)+" "+songs[i].getIndividualName());
				indLocs[countInds][1]=1;
				indLocs[countInds][2]=songs[i].getNumElements();
				indLocs[countInds][3]=songs[i].getNumPhrases();
				indLocs[countInds][4]=songs[i].getNumPhrases()-1;
				countInds++;
			}
		}
		individualSongs=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualSongs[i]=new int[indLocs[i][1]];
			int count2=0;
			for (int j=0; j<songNumber; j++){
				if (songs[j].getIndividualID()==indLocs[i][0]){
					individualSongs[i][count2]=j; 
					count2++;
				}
			}
		}
		individualTrans=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualTrans[i]=new int[indLocs[i][4]];
			int count2=0;
			for (int j=0; j<lookUpTrans.length; j++){
				int p=lookUpTrans[j][0];
				if (songs[p].getIndividualID()==indLocs[i][0]){
					individualTrans[i][count2]=j; 
					count2++;
				}
			}
		}
		individualSyl=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualSyl[i]=new int[indLocs[i][3]];
			int count2=0;
			for (int j=0; j<lookUpSyls.length; j++){
				int p=lookUpSyls[j][0];
				if (songs[p].getIndividualID()==indLocs[i][0]){
					individualSyl[i][count2]=j; 
					count2++;
				}
			}
		}
		individualEle=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualEle[i]=new int[indLocs[i][2]];
			int count2=0;
			for (int j=0; j<lookUpEls.length; j++){
				int p=lookUpEls[j][0];
				if (songs[p].getIndividualID()==indLocs[i][0]){
					individualEle[i][count2]=j; 
					count2++;
				}
			}
		}
		indLocs=null;
		individualNumber=countInds;
	}
	
	/**
	 * This function takes Element comparisons and outputs compressed Element comparisons (averages for all versions of an element within a phrase)
	 */
	
	public void compressElements(){
		if (scoresEle!=null){
			CompressComparisons cc=new CompressComparisons();
			scoresEleC=cc.compareElements(scoresEle, songs);
		}
	}
	
	/**
	 * This function compresses syllables - takes arrays of syllable comparisons, and outputs arrays of phrase comparisons
	 */
	
	public void compressSyllables(){
		CompressComparisons cc=new CompressComparisons();
		scoresSyll=cc.phraseComp(scoresEle, songs, (float)alignmentCost);		
	}
	
	/**
	 * this is what happens when you get both stitched and non-stitched syllable comparisons
	 * this method compares the two comparisons... and outputs the highest scoring (least similar) one
	 */
	
	public void compressSyllablesBoth(){

		if (scoresSyll2!=null){
			CompressComparisons cc=new CompressComparisons();
			scoresSyll2=cc.compareSyllables5(scoresSyll2, songs, alignmentCost);
		
			for (int i=0; i<scoresSyll.length; i++){
				for (int j=0; j<i; j++){
				System.out.println(i+" "+j+" "+scoresSyll[i][j]+" "+scoresSyll2[i][j]);
				scoresSyll[i][j]=(float)Math.max(scoresSyll[i][j], scoresSyll2[i][j]+stitchPunishment);
				}
			}
		}
	}
	
	/**
	 * This function compresses stitched syllable comparisons into phrase comparisons
	 */
	
	public void compressSyllablesStitch(){
		CompressComparisons cc=new CompressComparisons();
		scoresSyll=cc.compareSyllables5(scoresSyll2, songs, alignmentCost);
	}
	
	/**
	 * This function compresses syllable-phrase comparisons into transition comparison
	 */
	
	public void compressSyllableTransitions(){
		CompressComparisons cc=new CompressComparisons();
		scoreTrans=cc.compareSyllableTransitions2(scoresSyll, songs);
		
	}
	
	/**
	 * This function constructs song comparisons, based on phrase or transition comparisons
	 * @param useTrans this flags whether or not to use transition data (otherwise phrase data)
	 * @param lowerProp This is a parameter for a gating function
	 * @param upperProp This is a parameter for a gating function
	 */
	
	public void compressSongs(boolean useTrans, double lowerProp, double upperProp){
		try{
			System.out.println("Compressing songs");
			CompressComparisons cc=new CompressComparisons();
			//float[][]sy=logisticTransform(scoresSyll, true, 0.02, 10);
			//float[][]sy=gateScores(scoresSyll, 0.5, 20);
			if (!useTrans){
				scoresSong=cc.compareSongsDigram(scoresSyll, songs, useTrans);
			}
			else{
				scoresSong=cc.compareSongsDTW(scoresSyll, songs);	
			}
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	/**
	 * This method calculates the mean dissimilarity within a matrix
	 * @param score input triangular dissimilarity matrix
	 * @return mean dissimilarity
	 */
	
	public double getMatrixAv(float[][] score){
		double a=0;
		double b=0;
		for (int i=0; i<score.length; i++){
			for (int j=0; j<i; j++){
				a+=score[i][j];
				b++;
			}
		}
		double av=a/b;
		return av;
	}
	
	/**
	 * This method augments phrase dissimilarities by including a component related to their
	 * number of repetitions
	 */
	
	public void augmentSyllDistanceMatrixWithSyllableReps(){
		if (syllableRepetitions.length!=scoresSyll.length){
			System.out.println("AUGMENTATION FAILED: ERROR IN MATRIX SIZE");
		}
		else{
			double sd1=getMatrixAv(scoresSyll);
			//sd=syllableRepetitionWeighting*sd;
			float[][] tempMat=new float[syllableRepetitions.length][];
			for (int i=0; i<syllableRepetitions.length; i++){
				tempMat[i]=new float[i+1];
				for (int j=0; j<i; j++){
					double q=Math.log(syllableRepetitions[i])-Math.log(syllableRepetitions[j]);
					tempMat[i][j]=(float)Math.sqrt(q*q);
				}
			}
			double sd2=getMatrixAv(tempMat);
			//sd2=0.749591084;
			float weight=(float)(syllableRepetitionWeighting*sd1/sd2);
			System.out.println("WEIGHT: "+weight+" "+sd2);
			//weight=0.12161444f;
			//weight=0.05111025f;
			//weight=0.1022205f;
			for (int i=0; i<syllableRepetitions.length; i++){
				for (int j=0; j<i; j++){
					scoresSyll[i][j]+=weight*tempMat[i][j];
				}
			}
			
		}
	}
	
	/**
	 * This function is used to calculate the number of shared syllables within a dataset between two individuals
	 * @param a first individual id
	 * @param b second individual id
	 * @param threshold dissimilarity threshold
	 * @return number of shared syllables
	 */
	
	public int getSharedSyllCount(int a, int b, double threshold){
		int count=0;
		int counta=0;
		int countb=0;
		double mindiff=1000000000;
		for (int i=0; i<syllNumber; i++){
			//System.out.println(lookUpSyls[i][0]);
			if (lookUpSyls[i][0]==a){	
				counta++;
				int p=0;
				for (int j=0; j<i; j++){
					if (lookUpSyls[j][0]==b){
						countb++;
						if (scoresSyll[i][j]<threshold){
							p++;
						}
						if (scoresSyll[i][j]<mindiff){
							mindiff=scoresSyll[i][j];
						}
					}
				}
				for (int j=i; j<syllNumber; j++){
					if (lookUpSyls[j][0]==b){
						countb++;
						if (scoresSyll[j][i]<threshold){
							p++;
						}
						if (scoresSyll[j][i]<mindiff){
							mindiff=scoresSyll[j][i];
						}
					}
				}
				if (p>0){count++;}
				//count+=p;
			}
		}
		//System.out.println(syllNumber+" "+lookUpSyls.length+" "+scoresSyll.length);
		System.out.println("Syll sharing: "+a+" "+b+" "+counta+" "+countb+" "+count+" "+threshold+" "+mindiff);
		return count;
	}
	
	/**
	  * This function is used to calculate the number of shared transitions within a dataset between two individuals
	 * @param a
	 * @param b
	 * @param threshold
	 * @return
	 */
	
	public int getSharedTransCount(int a, int b, double threshold){
		int count=0;
		for (int i=0; i<transNumber; i++){
			if (lookUpTrans[i][0]==a){				
				for (int j=0; j<i; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[i][2]][lookUpTrans[j][2]]<threshold)&&(scoresSyll[lookUpTrans[i][3]][lookUpTrans[j][3]]<threshold)){
							count++;
						}
					}
				}
				for (int j=i; j<transNumber; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[j][2]][lookUpTrans[i][2]]<threshold)&&(scoresSyll[lookUpTrans[j][3]][lookUpTrans[i][3]]<threshold)){
							count++;
						}
					}
				}
			}
		}
		System.out.println("Trans sharing: "+a+" "+b+" "+count);
		return count;
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @param threshold
	 * @return
	 */
	
	public double getWeightedSharedTransCount(int a, int b, double threshold){
		double count=0;
		for (int i=0; i<transNumber; i++){
			//if ((lookUpTrans[i][0]==a)&&(lookUpTrans[i][1]>0)){		
			if (lookUpTrans[i][0]==a){	
				double c=0;
				for (int j=0; j<i; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[i][2]][lookUpTrans[j][2]]<threshold)&&(scoresSyll[lookUpTrans[i][3]][lookUpTrans[j][3]]<threshold)){
							c++;
						}
					}
				}
				for (int j=i; j<transNumber; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[j][2]][lookUpTrans[i][2]]<threshold)&&(scoresSyll[lookUpTrans[j][3]][lookUpTrans[i][3]]<threshold)){
							c++;
						}
					}
				}
				if (c>0){
					int x1=lookUpTrans[i][2];
					int x2=lookUpTrans[i][3];
					double d=0;
					
					for (int j=0; j<syllNumber; j++){
						if (lookUpSyls[j][0]==b){
							if ((j>x1)&&(scoresSyll[j][x1]<threshold)){
								d++;
							}
							if ((j<x1)&&(scoresSyll[x1][j]<threshold)){
								d++;
							}
							if ((j>x2)&&(scoresSyll[j][x2]<threshold)){
								d++;
							}
							if ((j<x2)&&(scoresSyll[x2][j]<threshold)){
								d++;
							}
						}
					}
					count+=2*c/d;
				}
			}
		}
		System.out.println("Weighted sharing: "+a+" "+b+" "+count);
		return count;
	}
	
	/**
	 * 
	 * @param label
	 * @param type
	 * @param ind
	 * @return
	 */
	
	public float[][] splitMatrix(int[] label, int type, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		float[][] results=new float[c][];
		float[][] scores=getScores(type);
		for (int i=0; i<c; i++){
			results[i]=new float[i+1];
		}
		
		int ii=0;
		int jj=0;
		for (int i=0; i<label.length; i++){
			jj=0;
			if (label[i]==ind){
				for (int j=0; j<i; j++){
					if (label[j]==ind){
						results[ii][jj]=scores[i][j];
						jj++;
					}
				}
				ii++;
			}
		}
		return results;
	}
	
	/**
	 * 
	 * @param label
	 * @param h
	 * @param ind
	 * @return
	 */
	
	public int[] splitCounts(int[] label, int h, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		int[] results=new int[c];
		
		int ii=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){
				if (h==0){
					results[ii]=eleCounts[i];
				}
				else if (h==1){
					results[ii]=eleCounts[i];
				}
				else if (h==2){
					results[ii]=sylCounts[i];
				}
				ii++;
			}
		}
		return results;
	}
	
	/**
	 * This function looks up a subset of the song data based on label and ind input data
	 * @param label int[] of values for each song
	 * @param ind an int value to look up in label
	 * @return an int[][] containing song ids for each individual in the subset
	 */
	
	public int[][] splitIndSongs(int[] label, int ind){
		int c=0;
		for (int i=0; i<individualSongs.length; i++){
			int j=individualSongs[i][0];
			if (label[j]==ind){c++;}
		}
		
		int[][] results=new int[c][];
		
		int[] mat=new int[label.length];
		int d=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){
				mat[i]=d;
				d++;
			}
			else{
				mat[i]=-1;
			}
		}
		
		int ii=0;
		for (int i=0; i<individualSongs.length; i++){
			int j=individualSongs[i][0];
			if (label[j]==ind){
				results[ii]=new int[individualSongs[i].length];
				for (int k=0; k<individualSongs[i].length; k++){
					results[ii][k]=mat[individualSongs[i][k]];
				}
				ii++;
			}
		}
		return results;
	}
	
	/**
	 * This function extracts from lookup tables all data of a particular class, by matching
	 * an int, ind to an int[] array label. 
	 * @param label int[] array to match data to
	 * @param type hierarchical level to use (from Element to Song)
	 * @param ind label to look for
	 * @return an int[][] of lookUps for a particular subclass of the dataset
	 */
	
	public int[][] splitLookUps(int[] label, int type, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		int[][] results=new int[c][];
		int[][] lookUp=getLookUp(type);
		
		int ii=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){
				results[ii]=new int[lookUp[i].length];
				System.arraycopy(lookUp[i], 0, results[ii], 0, lookUp[i].length);
				ii++;
			}
		}
		return results;
	}
	
	
	/**
	 * This function cleans up some of the large objects in Analysis when an analysis is complete
	 */
	
	public void cleanUp(){
		songs=null;
		//data=null;
		individualSongs=null;
		individualTrans=null;
		individualSyl=null;
		individualEle=null;
		lookUpEls=null;
		lookUpSyls=null;
		lookUpTrans=null;
		lookUpSongs=null;
		//scoresEle=null;
		//scoresEleC=null;
		//scoresSyll=null;
		//scoresSyll2=null;
		//scoreTrans=null;
		//scoresSong=null;
		compScheme=null;
	}
	
}
