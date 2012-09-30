#!/usr/bin/perl
#
# mysql2ssc v0.1 by Eric Barch
#
# This application is designed for driving a robot using a Mini SSC II
# It reads values from the database and then writes them out to the serial port
# If the data becomes stale, all outputs are set to neutral

use FileHandle;
use DBI();
use Time::HiRes qw ( sleep );

# Connect to the database.
my $dbh = DBI->connect("DBI:mysql:database=robot;host=localhost","root", "ttj",{'RaiseError' => 1});

# Define the serial port
$serial_device = "/dev/ttyS0";

# Initiate main program loop
while (1)
{
	# Now retrieve data from the table.
  	my $sth = $dbh->prepare("SELECT Ldrive FROM data WHERE id = 1");
  	$sth->execute();
  	while (my $ref = $sth->fetchrow_hashref()) {
    		$Ldrive = $ref->{'Ldrive'};
  	}
  	$sth->finish();

	# Now retrieve data from the table.
  	my $sth = $dbh->prepare("SELECT Rdrive FROM data WHERE id = 1");
  	$sth->execute();
  	while (my $ref = $sth->fetchrow_hashref()) {
    		$Rdrive = $ref->{'Rdrive'};
  	}
  	$sth->finish();

	# Now retrieve data from the table.
  	my $sth = $dbh->prepare("SELECT lastupdate FROM data WHERE id = 1");
  	$sth->execute();
  	while (my $ref = $sth->fetchrow_hashref()) {
    		$record = $ref->{'lastupdate'};
  	}
  	$sth->finish();

	# Open up the serial port
	open(SERIAL,"+< $serial_device") or die "open: $!";
	SERIAL->autoflush(1);

	# We haven't received a new command lately, set outputs to neutral
	if ((time - $record) > 1)
	{
		# Setting all PWM values to neutral
		printf(SERIAL "%c%c%c",255,0,127);
		printf(SERIAL "%c%c%c",255,1,127);
		printf(SERIAL "%c%c%c",255,2,127);
		printf(SERIAL "%c%c%c",255,3,127);
		printf(SERIAL "%c%c%c",255,4,127);
		printf(SERIAL "%c%c%c",255,5,127);
		printf(SERIAL "%c%c%c",255,6,127);
		printf(SERIAL "%c%c%c",255,7,127);

		# Print diagnostic data
		print "Control data stale. Sending neutral to all outputs.\n";
	}
	elsif ($Ldrive >= 0 && $Ldrive <= 254 && $Rdrive >= 0 && $Rdrive <= 254)
	{
		# We've got valid values!
		# Send data provided to script from serial port
		printf(SERIAL "%c%c%c",255,0,$Ldrive);
		printf(SERIAL "%c%c%c",255,1,$Rdrive);

		# Print diagnostic data
		print "Outputs sent to Mini SSC.\n";
	}	
	else
	{
		print "Invalid values provided.\n";
	}

	# Close the serial port
	close(SERIAL);

	# Delay for 5ms
	sleep (.005);
}