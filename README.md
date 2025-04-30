# Telescope Simulation
  This application is a Java-based implementation of a simple telescope simulator.
  The web-based GUI allows the user to interact with the telescope and it's devices.

### Telescope
  The telescope can have multiple devices, each of which have a motor and a receiver. These
  devices can then be sent to a particular co-ordinate, and set to receive mode. 

### Sky Simulation
  A sky simulation has been included, which can programatically be changed (no interface), but would allow
  the user to make the sky domain bigger and add add stars.

### Functionality
  Some basic state changes in the telescope operation and some control functionality have been included.
  Multiple devices can be added and independantly controlled.
  Artifical errors can be induced.

### Unit Tests
  Some basic unit tests to verify core functionality have been included

## To Run
  Start the Java application (run the Main class). Once the server is running, navigate to:
  
  <em>http://localhost:8080/</em>
  
  This should load the local index.html page that contains the GUI
