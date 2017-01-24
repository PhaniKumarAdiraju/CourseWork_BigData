import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * @author Phanikumar A
 *
 * 
 */

public class PairCount1 {

	public static TreeSet<ResultPair> sortedTreePair = new TreeSet<>();

	public static class Map extends Mapper<LongWritable, Text, Text, LongWritable> {
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			String[] words = value.toString().split("\\s+");
			for (String word : words) {
				if (word.matches("^\\w+$")) {
					context.write(new Text(word.trim() + " " + "&"), new LongWritable(1));
				}
			}
			StringBuilder sb = new StringBuilder();
			for (int index = 0; index < words.length; index++) {
				if (index == words.length - 1) {
					break;
				} else {
					if (words[index].matches("^\\w+$") && words[index + 1].matches("^\\w+$")) {
						sb.append(words[index]).append(" ").append(words[index + 1]);
						context.write(new Text(sb.toString()), new LongWritable(1));
						sb.delete(0, sb.length());
					}
				}
			}

		}

	}

	public static class Combiner extends Reducer<Text, LongWritable, Text, LongWritable> {

		public void reduce(Text key, Iterable<LongWritable> values, Context context)
				throws IOException, InterruptedException {

			long count = 0;
			for (LongWritable val : values) {
				count += val.get();
			}
			context.write(key, new LongWritable(count));
		}

	}

	public static class Reduce extends Reducer<Text, LongWritable, Text, Text> {

		private DoubleWritable totalCount = new DoubleWritable();
		private DoubleWritable relativeCount = new DoubleWritable();
		private Text currentWord = new Text("NOT_YET_SET");

		public void reduce(Text key, Iterable<LongWritable> values, Context context)
				throws IOException, InterruptedException {

			if (key.toString().split(" ")[1].equals("&")) {
				if (key.toString().split(" ")[0].equals(currentWord.toString())) {
					totalCount.set(totalCount.get() + getTotalCount(values));
				} else {
					currentWord.set(key.toString().split(" ")[0]);
					totalCount.set(0);
					totalCount.set(getTotalCount(values));
				}
			} else {

				double count = getTotalCount(values);
				if (count != 1) {
					relativeCount.set((double) count / totalCount.get());
					Double relativeCountD = relativeCount.get();
					
					if(relativeCountD ==1.0d){

					sortedTreePair.add(new ResultPair(relativeCountD,count, key.toString(), currentWord.toString()));

					if (sortedTreePair.size() > 100) {
						sortedTreePair.pollLast();
					}

					context.write(key, new Text(Double.toString(relativeCountD)));}
				}
			}
		}

		private double getTotalCount(Iterable<LongWritable> values) {
			double count = 0;
			for (LongWritable value : values) {
				count += value.get();
			}
			return count;
		}

	}

	public static void main(String[] args) throws Exception {
		Job job = Job.getInstance(new Configuration());
		job.setJarByClass(PairCount1.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setCombinerClass(Combiner.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LongWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);

		job.waitForCompletion(true);

		File f = new File(args[1] + "/top100.txt");
		f.createNewFile();
		FileWriter fr = new FileWriter(f);
		for (ResultPair v : sortedTreePair) {
			fr.write(v.key + " : " + v.value + " : " + v.relativeFrequency + "\n");
		}
		fr.close();

		System.out.println("=====treemap output===");

		for (ResultPair v : sortedTreePair) {
			System.out.println(v.key + " : " + v.value + " : " + v.relativeFrequency);

		}

	}

	public static class ResultPair implements Comparable<ResultPair> {
		double relativeFrequency;
		double count;
		String key;
		String value;

		ResultPair(double relativeFrequency, double count, String key, String value) {
			this.relativeFrequency = relativeFrequency;
			this.count = count;
			this.key = key;
			this.value = value;
		}

		@Override
		public int compareTo(ResultPair resultPair) {
			
				if(this.count<=resultPair.count){
				
				return 1;
			} else {
				return -1;
			}
			
		}
	}

}
