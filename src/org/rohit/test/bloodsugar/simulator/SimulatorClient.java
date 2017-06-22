package org.rohit.test.bloodsugar.simulator;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.rohit.test.bloodsugar.model.InputEntry;
import org.rohit.test.bloodsugar.model.OutputPoint;


/**
 * This is a client class that will call the Simulator engine
 * This will pass the inputs and will make result in getting the graph drawn
 * @author Frozenfire
 *
 */
public class SimulatorClient extends JFrame{
	
	static SimulatorEngine engine = new SimulatorEngine();
	
	public SimulatorClient() {
		super("Blood Sugar Simulator");

		JPanel chartPanel = createChartPanel();
		add(chartPanel, BorderLayout.CENTER);

		setSize(640, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	private JPanel createChartPanel() {
		/*String chartTitle = "Blood Sugar Simulator";
		String xAxisLabel = "X";
		String yAxisLabel = "Y";

		XYDataset dataset = createDataset();

		JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, 
				xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, rootPaneCheckingEnabled, rootPaneCheckingEnabled, rootPaneCheckingEnabled);

		return new ChartPanel(chart);
		
*/

        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Blood Sugar Chart",
            "Time", 
            "Value",
            createDataset(),
            true,
            true,
            false
        );

        chart.setBackgroundPaint(Color.white);
        
//        final StandardLegend sl = (StandardLegend) chart.getLegend();
  //      sl.setDisplaySeriesShapes(true);

        final XYPlot plot = chart.getXYPlot();
        //plot.setOutlinePaint(null);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(false);
        
        final XYItemRenderer renderer = plot.getRenderer();
        if (renderer instanceof StandardXYItemRenderer) {
            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
            //rr.setPlotShapes(true);
            //rr.setShapesFilled(true);
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            renderer.setSeriesStroke(1, new BasicStroke(2.0f));
           }
        
        final DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("hh:mma"));
        
        return new ChartPanel(chart);
	}
	
	private XYDataset createDataset() {
		
		
		List<OutputPoint> output = engine.getBloodSugarList();
		
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		final TimeSeries s1 = new TimeSeries("Blood Sugar", Minute.class);
		
		Calendar calendar = GregorianCalendar.getInstance();
		
		for(OutputPoint out:output)
		{
			calendar.setTime(out.getTimestamp());
			s1.add(new Minute(calendar.get(Calendar.MINUTE), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.DAY_OF_MONTH),
					calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)), out.getValue());
		
		}
        
        dataset.addSeries(s1);
        return dataset;
	}


	public static void main(String[] args) {
		
		createInputs();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SimulatorClient().setVisible(true);
			}
		});
	}
	
	public static void createInputs()
	{
		List<InputEntry> list = new ArrayList();
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY,12);
		cal.set(Calendar.MINUTE,30);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		Date d = cal.getTime();
		InputEntry entry = new InputEntry();
		entry.setTimestamp(d);
		entry.setItem("A");
		entry.setType("EX");
		
		list.add(entry);
		
		cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY,4);
		cal.set(Calendar.MINUTE,15);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		d = cal.getTime();
		entry = new InputEntry();
		entry.setTimestamp(d);
		entry.setItem("B");
		entry.setType("FD");
		
		list.add(entry);
		
		engine.processUserInputs(list);
	}

}
