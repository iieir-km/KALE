package kale.struct;

public class Rule {
	private Triple FstTriple=null;
	private Triple SndTriple=null;
	private Triple TrdTriple=null;
	
	public Rule(Triple inFstTriple, Triple inSndTriple) {
		FstTriple = inFstTriple;
		SndTriple = inSndTriple;
	}
	public Rule(Triple inFstTriple, Triple inSndTriple,Triple inTrdTriple) {
		FstTriple = inFstTriple;
		SndTriple = inSndTriple;
		TrdTriple = inTrdTriple;
	}
	
	public Triple fstTriple() {
		return FstTriple;
	}
	
	public Triple sndTriple() {
		return SndTriple;
	}
	
	public Triple trdTriple() {
		return TrdTriple;
	}
	
}
