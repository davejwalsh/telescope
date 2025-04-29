import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.dto.StatusCode;
import com.ska.telescopeSimulator.service.DeviceService;
import com.ska.telescopeSimulator.sky.SimulatedSky;
import com.ska.telescopeSimulator.sky.SimulatedStar;
import com.ska.telescopeSimulator.states.StatusResponse;
import com.ska.telescopeSimulator.web.WebsocketController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class UnitTests {

    @Test
    void testSetSelectedDevice() {
        DeviceService service = getDeviceService();
        service.addNewDevice();
        StatusResponse response = service.setSelectedDevice(1);
        assertEquals(StatusCode.SUCCESS, response.getStatus());
        assertEquals("Device 1 selected", response.getMessage());
    }

    @Test
    void testSetTargetCoordinates() {
        DeviceService service = getDeviceService();
        service.startSelectedDevice();
        DeviceCoordinates targetCoordinates = new DeviceCoordinates(2.0,3.0,4.0);
        StatusResponse response = service.setTargetCoordinatesForSelectedDevice(targetCoordinates);
        assertEquals(StatusCode.SUCCESS, response.getStatus());
        DeviceCoordinates currentCoordinates = service.getSetTargetCoordinates();
        assertEquals(currentCoordinates, targetCoordinates);
    }


    @Test
    void testSetTargetCoordinatesOnMultipleDevices() {
        DeviceService service = getDeviceService();
        DeviceCoordinates deviceOneTargetCoordinates = new DeviceCoordinates(2.0,3.0,4.0);
        DeviceCoordinates deviceTwoTargetCoordinates = new DeviceCoordinates(10.0,20.0,30.0);

        service.addNewDevice();
        service.setSelectedDevice(0);
        service.startSelectedDevice();
        service.setTargetCoordinatesForSelectedDevice(deviceOneTargetCoordinates);

        service.setSelectedDevice(1);
        service.startSelectedDevice();
        service.setTargetCoordinatesForSelectedDevice(deviceTwoTargetCoordinates);

        service.setSelectedDevice(0);
        assertEquals(service.getSetTargetCoordinates(), deviceOneTargetCoordinates);

        service.setSelectedDevice(1);
        assertEquals(service.getSetTargetCoordinates(), deviceTwoTargetCoordinates);
    }

    @Test
    void testSetTargetWithDeviceOff(){
        DeviceService service = getDeviceService();
        DeviceCoordinates deviceOneTargetCoordinates = new DeviceCoordinates(1.0,1.0,1.0);
        StatusResponse setResponse = service.setTargetCoordinatesForSelectedDevice(deviceOneTargetCoordinates);
        assertEquals(StatusCode.FAIL, setResponse.getStatus());
//        StatusResponse moveResponse = service.moveToTargetCoordinatesForSelectedDevice();
//        assertEquals(StatusCode.FAIL, moveResponse.getStatus());
    }


    @Test
    void testMoveMotorWithDeviceOff(){
        DeviceService service = getDeviceService();
        DeviceCoordinates deviceOneTargetCoordinates = new DeviceCoordinates(1.0,1.0,1.0);
        service.startSelectedDevice();
        StatusResponse setResponse = service.setTargetCoordinatesForSelectedDevice(deviceOneTargetCoordinates);
        assertEquals(StatusCode.SUCCESS, setResponse.getStatus());
        service.stopSelectedDevice();
        StatusResponse moveResponse = service.moveToTargetCoordinatesForSelectedDevice();
        assertEquals(StatusCode.FAIL, moveResponse.getStatus());
    }
    @Test
    void testMoveMotor() throws InterruptedException {
        DeviceService service = getDeviceService();
        DeviceCoordinates deviceOneTargetCoordinates = new DeviceCoordinates(1.0,1.0,1.0);
        service.startSelectedDevice();
        StatusResponse setResponse = service.setTargetCoordinatesForSelectedDevice(deviceOneTargetCoordinates);
        assertEquals(StatusCode.SUCCESS, setResponse.getStatus());
        StatusResponse moveResponse = service.moveToTargetCoordinatesForSelectedDevice();
        assertEquals(StatusCode.SUCCESS, moveResponse.getStatus());

        Thread.sleep(2000);

        assertEquals(service.getCurrentlySelectedDeviceCoordinates(), deviceOneTargetCoordinates);
    }

    @Test
    void testMoveMotorMultipleDevices() throws InterruptedException {
        DeviceService service = getDeviceService();
        DeviceCoordinates deviceOneTargetCoordinates = new DeviceCoordinates(1.0,1.0,1.0);
        DeviceCoordinates deviceTwoTargetCoordinates = new DeviceCoordinates(2.0,2.0,2.0);

        service.startSelectedDevice();
        StatusResponse setDeviceOneResponse = service.setTargetCoordinatesForSelectedDevice(deviceOneTargetCoordinates);
        assertEquals(StatusCode.SUCCESS, setDeviceOneResponse.getStatus());


        service.addNewDevice();
        service.setSelectedDevice(1);
        service.startSelectedDevice();

        StatusResponse setDeviceTwo = service.setTargetCoordinatesForSelectedDevice(deviceTwoTargetCoordinates);
        assertEquals(StatusCode.SUCCESS, setDeviceTwo.getStatus());

        service.setSelectedDevice(0);
        StatusResponse moveResponseDeviceOne = service.moveToTargetCoordinatesForSelectedDevice();
        assertEquals(StatusCode.SUCCESS, moveResponseDeviceOne.getStatus());

        service.setSelectedDevice(1);
        StatusResponse moveResponseDeviceTwo = service.moveToTargetCoordinatesForSelectedDevice();
        assertEquals(StatusCode.SUCCESS, moveResponseDeviceTwo.getStatus());

        Thread.sleep(4000);

        service.setSelectedDevice(0);
        assertEquals(service.getCurrentlySelectedDeviceCoordinates(), deviceOneTargetCoordinates);
        service.setSelectedDevice(1);
        assertEquals(service.getCurrentlySelectedDeviceCoordinates(), deviceTwoTargetCoordinates);
    }

    @Test
    void testAttemptMovementWhenApetureOpen() throws InterruptedException {
        DeviceService service = getDeviceService();
        DeviceCoordinates deviceOneTargetCoordinates = new DeviceCoordinates(1.0,1.0,1.0);
        DeviceCoordinates deviceTwoTargetCoordinates = new DeviceCoordinates(2.0,2.0,2.0);

        service.startSelectedDevice();
        service.setTargetCoordinatesForSelectedDevice(deviceOneTargetCoordinates);
        service.moveToTargetCoordinatesForSelectedDevice();
        Thread.sleep(2000);
        assertEquals(service.getCurrentlySelectedDeviceCoordinates(), deviceOneTargetCoordinates);

        StatusResponse shutterTimeSetResponse = service.setShutterTime(3.0);
        assertEquals(StatusCode.SUCCESS, shutterTimeSetResponse.getStatus());

        StatusResponse startReceivingResponse = service.startReceivingSelectedDevice();
        assertEquals(StatusCode.SUCCESS, startReceivingResponse.getStatus());

        service.setTargetCoordinatesForSelectedDevice(deviceTwoTargetCoordinates);
        StatusResponse movementResponse = service.moveToTargetCoordinatesForSelectedDevice();
        assertEquals(StatusCode.FAIL, movementResponse.getStatus());
    }

    /***
     *  Utility methods for initialising a Simulated Sky and creting a DeviceService with
     *  a mocked WebsocketController
     *
     * @return
     */

    private SimulatedSky getSimulatedSky(){
        return new SimulatedSky(getStars());
    }

    private ArrayList<SimulatedStar> getStars(){
        SimulatedStar starOne =  new SimulatedStar(new DeviceCoordinates(20.0, 20.0, 20.0), 20.0, 5.0);
        SimulatedStar starTwo =  new SimulatedStar(new DeviceCoordinates(80.0, 80.0, 80.0), 40.0, 8.0);
        ArrayList<SimulatedStar> stars = new ArrayList<>();
        stars.add(starOne);
        stars.add(starTwo);
        return stars;
    }

    private WebsocketController getWebsocketController(){
        SimpMessagingTemplate mockTemplate = Mockito.mock(SimpMessagingTemplate.class);
        return new WebsocketController(mockTemplate);
    }

    private DeviceService getDeviceService(){
        return new DeviceService(getSimulatedSky(), getWebsocketController());
    }

}
