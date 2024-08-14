package cmsc433.p5;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/**
 * Map reduce which takes in a CSV file with tweets as input and output
 * key/value pairs.</br>
 * </br>
 * The key for the map reduce depends on the specified {@link TrendingParameter}
 * , <code>trendingOn</code> passed to
 * {@link #score(Job, String, String, TrendingParameter)}).
 */
public class TweetPopularityMR {
	public static int check = 0;
	// For your convenience...
	public static final int          TWEET_SCORE   = 1;
	public static final int          RETWEET_SCORE = 3;
	public static final int          MENTION_SCORE = 1;
	public static final int			 PAIR_SCORE = 1;

	// Is either USER, TWEET, HASHTAG, or HASHTAG_PAIR. Set for you before call to map()
	private static TrendingParameter trendingOn;



	public static class TweetMapper
	extends Mapper<Object, Text, Text, IntWritable> {

		@Override
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			// Converts the CSV line into a tweet object
			Tweet tweet = Tweet.createTweet(value.toString());
			
			System.out.println("THESE ARE THE HASHTAGS" + tweet.getHashtags());
			String retUser = tweet.getRetweetedUser();
			// TODO: Your code goes here

			if (trendingOn == TrendingParameter.USER) {
				for (String tweeter : tweet.getMentionedUsers()) {
					context.write(new Text(tweeter), new IntWritable(1));
				}
				if (retUser != null) {
					context.write(new Text(retUser) , new IntWritable(3));
				}
				context.write(new Text(tweet.getUserScreenName()), new IntWritable(1));

			}
			else if (trendingOn == TrendingParameter.HASHTAG) {
				for (String hash : tweet.getHashtags()) {
					String hashRev = hash.replace("#", "");
					context.write(new Text(hashRev.toString()), new IntWritable(1));
				}
			}
			else if (trendingOn == TrendingParameter.TWEET) {
				context.write(new Text(tweet.getId().toString()),  new IntWritable(1));
				if (tweet.wasRetweetOfTweet()) {
					context.write(new Text(tweet.getRetweetedTweet().toString()), new IntWritable(3));
				}
			}
			else if (trendingOn == TrendingParameter.HASHTAG_PAIR) {
				if (tweet.getHashtags().size() >= 2) {
					ArrayList<String> pairs = HashTagPairCreator(tweet.getHashtags());
					for (String s: pairs) {
						context.write(new Text(s), new IntWritable(1));
					}
				}
			}




		}
	}



	private static  ArrayList<String> HashTagPairCreator(List<String> hashTags) {

		

		ArrayList<String> plist = new ArrayList<String>();
		ArrayList<String> finalList = new ArrayList<String>();
		
		
		for (String s : hashTags) {
			plist.add(s.replace("#", ""));
		}
		if (plist.contains("androidgames") && plist.contains("gameinsight")) {
			//check++;
		
		//	System.out.println(check);
		}
	
		if (plist.size() < 2) {
			return plist;
		}
		for (int i = 0; i < hashTags.size(); i++ ) {
			for (int j = i+1; j < hashTags.size(); j++) {
				

				if (plist.get(i).compareTo(plist.get(j)) <= 0) {
					finalList.add("(" + plist.get(j) + "," + plist.get(i) + ")");
				
				}
				else {
					finalList.add("(" + plist.get(i) + "," + plist.get(j) + ")");
					
				}
			
			
			}
		}
	
		return finalList;
	}



		public static class PopularityReducer
		extends Reducer<Text, IntWritable, Text, IntWritable> {

			@Override
			
			
			public void reduce(Text key, Iterable<IntWritable> values, Context context)
					throws IOException, InterruptedException {
				
				int num = 0;
				for (IntWritable value : values) {
					num += value.get();
				}
				
				context.write(new Text(key.toString()), new IntWritable(num));
				System.out.println(context);




			}
		}

		/**
		 * Method which performs a map reduce on a specified input CSV file and
		 * outputs the scored tweets, users, or hashtags.</br>
		 * </br>
		 * 
		 * @param job
		 * @param input
		 *          The CSV file containing tweets
		 * @param output
		 *          The output file with the scores
		 * @param trendingOn
		 *          The parameter on which to score
		 * @return true if the map reduce was successful, false otherwise.
		 * @throws Exception
		 */
		public static boolean score(Job job, String input, String output,
				TrendingParameter trendingOn) throws Exception {

			TweetPopularityMR.trendingOn = trendingOn;

			job.setJarByClass(TweetPopularityMR.class);

			// TODO: Set up map-reduce...
			job.setJobName("score Map");
			
			job.setMapperClass(TweetMapper.class);
			job.setReducerClass(PopularityReducer.class);
			
			job.setMapOutputKeyClass(Text.class);
			job.setMapOutputValueClass(IntWritable.class);
			
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);


			// End
			System.out.println("the input is " + input);

			FileInputFormat.addInputPath(job, new Path(input));
			FileOutputFormat.setOutputPath(job, new Path(output));

			return job.waitForCompletion(true);
		}
	}