# Blood Sugar Simulator
Tool to plot the blood sugar and glycation on a graph for a given set of Inputs. 
Inputs will be either Food or Excercise along with the timestamp of each event.

# What does it do?

1. It will increase the blood sugar based on the glycemic index of the food input for 2 hours from the time of entry
1. It will decrease the blood sugar based on the glycemic index of the excercise input for 1 hour from the time of entry
1. It will normalise the blood sugar at the rate of 1/min if the food or excercise is not impacting the blood sugar
1. It will track the glycation value over the entire day, and increase it by 1 for every minute blood sugar stays over 150
1. It will plot the blood sugar along with glycation graphs in the output


# How to run it?
##Locally

1. Download the project
1. Create a new Maven Project in eclipse
1. Right click, select configure and Select Convert to maven project
1. Import the downloaded project into this Workspace project
1. Replace the pom.xml and src folder in workspace folder with the files in downloaded project
1. Clean and build the project
1. Run SimulatorCLient.java as a Java application
1. To modify the input points, change the entries in the main function of the SimulatorClient.java 
1. Input will of the form "Item""Type""Hours""Min""Sec"

##Docker
1. Make sure you have Docker on your system
1. Make sure you have X11 or XQuartz depending on your OS to stream the graph output
1. Docker commands coming soon....
