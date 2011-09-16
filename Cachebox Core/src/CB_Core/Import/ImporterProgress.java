package CB_Core.Import;

import java.util.ArrayList;

import CB_Core.Events.ProgresssChangedEventList;

/**
 * Verwaltet den Progress Status beim Importieren
 * @author Longri
 *
 */
public class ImporterProgress 
{

	/**
	 * Enthält einen Übergeordneten Schritt
	 * @author Longri
	 *
	 */
	public class Step
	{
		public Step(String Name, float weight)
		{
			this.weight=weight;
			this.Name=Name;
			this.progress=0.0f;
		}
		
		public float weight=0.0f;
		public float progress=0.0f;
		public String Name;
		public float stepweight;
		
		public void setMaxStep(int max)
		{
			if(max==0)
			{
				this.stepweight = 1f;
			}
			else
			{
				this.stepweight = 1f/(float)max;
			}
			
		}
	}
	
	
	
	private ArrayList<Step> steps;
	private float weightSumme=0.0f;
	
	
	//Initial Progress at Constructor
	public ImporterProgress()
	{
		steps = new ArrayList<Step>();
		steps.add(new Step("importGC",0));
		steps.add(new Step("importMail",0));
		steps.add(new Step("ExtractZip",3));
		steps.add(new Step("IndexingDB",2));
		steps.add(new Step("ImportGPX",4));
		steps.add(new Step("WriteCachesToDB",4));
		steps.add(new Step("WriteLogsToDB",4));
		steps.add(new Step("WriteWaypointsToDB",4));
		steps.add(new Step("importGcVote",0));
		
		weightSumme= getWeightSumm();
	}
	
	private float getWeightSumm() 
	{
		float sum=0.0f;
		for (Step job : steps)
		{
			sum += job.weight;
		}
		return sum;
	}

	public void ProgressInkrement(String Name, String Msg)
	{
		//get Job
		int Progress=0;
		for (Step job : steps)
		{
			if(job.Name.equals(Name))
			{
				job.progress +=job.stepweight;
				Progress = getProgress();
				
				break;
			}
		}
		
		// send Progress Change Msg
		ProgresssChangedEventList.Call(Name, Msg, Progress);
	}
	
	public void setJobMax(String Name, int max)
	{
		for (Step job : steps)
		{
			if(job.Name.equals(Name))
			{
				job.setMaxStep(max);
			}
		}
	}

	private int getProgress() 
	{
		float progress=0.0f;
		
		for (Step job : steps)
		{
			progress+= (job.weight/weightSumme)*job.progress;
		}
			
		return (int) (100*progress);
	}
	
}
