import minimalmodbus

serialPort = '/dev/ttyUSB1'
slaveAddress = 1
register = 289


''' reads a register on modubs

    port(string) - usb port to use
    addr(int) - slave address
    reg(int) - the register number to query
    decimalPlaces(int) - the number of decimalPlaces expected

    returns the result
'''
def ReadModbus(port, addr, reg, decimalPlaces):
    instrument = minimalmodbus.Instrument(port, addr) # port name, slave address (in decimal)

    ## Read temperature (PV = ProcessValue) ##
    retVal = instrument.read_register(reg, decimalPlaces) # Registernumber, number of decimals

    return retVal

''' writes to a register on modubs

    port(string) - usb port to use
    addr(int) - slave address
    reg(int) - the register number to query
    decimalPlaces(int) - the number of decimalPlaces expected
    value(int) - the value to write to the register
'''
def ReadModbus(port, addr, reg, decimalPlaces, value):
    instrument = minimalmodbus.Instrument(port, addr) # port name, slave address (in decimal)

    instrument.write_register(reg, value, decimalPlaces) # Registernumber, value, number of decimals for storage


# test code
val = ReadModbus(serialPort, slaveAddress, register, 0)

print(val)
