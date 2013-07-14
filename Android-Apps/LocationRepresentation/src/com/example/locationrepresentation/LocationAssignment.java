package com.example.locationrepresentation;

import java.util.ArrayList;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class LocationAssignment extends Activity{
	private Button done;
	private Spinner homes, floors, rooms, inrooms;
	private TextView detail;
	private EditText usertags;
	private String serviceid;
	private String info;
	private BroadcastReceiver receiver;
	private ArrayList<String> homeList;
	private ArrayList<String> floorList;
	private ArrayList<String> roomList;
	private ArrayList<String> inRoomList = new ArrayList<String>();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_location);

		inRoomList.add("Top");
		inRoomList.add("Bottom");
		inRoomList.add("Right");
		inRoomList.add("Left");
		inRoomList.add("Front");
		inRoomList.add("Back");

		info = getIntent().getStringExtra("Device");
		serviceid = info.split(" ")[1];

		// do initialization of the view
		init();
		
		detail.setText(info);
		// send get info action to all the devices
		Intent findDevice = new Intent(LocationAssignment.this, RegistrationService.class);
		findDevice.putExtra("command", "GETLOCATION");
		startService(findDevice);

	}

	/**
	 * Do initialization of the views
	 */
	private void init() {
		done = (Button) findViewById(R.id.setlocationbutton);
		homes = (Spinner) findViewById(R.id.spinner1);
		rooms = (Spinner) findViewById(R.id.spinner2);
		floors = (Spinner) findViewById(R.id.spinner3);
		inrooms = (Spinner) findViewById(R.id.spinner4);
		detail = (TextView) findViewById(R.id.serviceid);
		usertags = (EditText) findViewById(R.id.usertag);
		
		// TODO
		done.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// first get what the user has given
				String home = homes.getSelectedItem().toString();
				String floor = floors.getSelectedItem().toString();
				String room = rooms.getSelectedItem().toString();
				String inroom = inrooms.getSelectedItem().toString();
				String usertag = usertags.getText().toString();
				
				// send setLocation to the selected device
				Intent setLocation = new Intent(LocationAssignment.this, RegistrationService.class);
				setLocation.putExtra("command", "SETLOCATION");
				setLocation.putExtra("dstServiceid", serviceid);
				setLocation.putExtra("location", home+"+"+floor+"+"+room+"+"+inroom+"+"+usertag);
				startService(setLocation);
				
				// store the user tag into the database
				Intent addUserTag = new Intent(LocationAssignment.this, RegistrationService.class);
				addUserTag.putExtra("command", "ADDUSERTAG");
				addUserTag.putExtra("USERTAG", usertag);
				startService(addUserTag);
			}
		});

		receiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				homeList = intent.getStringArrayListExtra("homes");
				floorList = intent.getStringArrayListExtra("floors");
				roomList = intent.getStringArrayListExtra("rooms");
				populateHome();
				populateFloors();
				populateRooms();
				populateInroom();
			}
		};
	}

	private void populateHome() {

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, homeList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		homes.setAdapter(dataAdapter);

	}

	private void populateRooms() {

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, roomList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		rooms.setAdapter(dataAdapter);

	}

	private void populateFloors() {

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, floorList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		floors.setAdapter(dataAdapter);

	}	

	private void populateInroom() {
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, inRoomList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		inrooms.setAdapter(dataAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter("ASSIGNLOCATION"));
	}

	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		super.onStop();
	}

}
