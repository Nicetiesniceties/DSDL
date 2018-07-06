import bluetooth
device = bluetooth.discover_devices(lookup_names = True)
# print (device)

socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
print (socket)
port = bluetooth.PORT_ANY

socket.bind(("", port))

socket.listen(1)
uuid = "00001101-0000-1000-8000-00805F9B34FB"
bluetooth.advertise_service(socket, "apple",service_id = uuid,
    service_classes = [uuid, bluetooth.SERIAL_PORT_CLASS], profiles = [bluetooth.SERIAL_PORT_PROFILE])

#bluetooth.advertise_service(socket, "tt",service_id
#bluetooth.advertise_service(socket, "tt",service_id = uuid)
#bluetooth.advertise_service(socket, "hello")

#{{{
passward, initialized = "6666", False
#}}}

while 1 :
    try:
        print "Awaiting Connection..."
        client_sock, address = socket.accept()
        print "Connected with", address[0], "\n"
        while True :
            if(not initialized):
                print "Verifying the password..."
                msg = "Please send the password"
                client_sock.sendall(msg.encode())
                recv_data = client_sock.recv(1024)
                recv_data = recv_data.decode('utf-8')
                if(recv_data == passward):
                    send_data = "Correct Passward"
                    print "Password verified!\n"
                else:
                    send_data = "Wrong Passward"
                    print "Password not verified.\n"
                send_data = send_data.encode()
                client_sock.sendall(send_data)
                initialized = True

            print "Recieving the message..."
            recv_data = client_sock.recv(1024)
            recv_data = recv_data.decode('utf-8')
            print address[0], ":", recv_data, ".\n"

            send_data = raw_input('Enter your message: ')
            send_data = send_data.encode()
            client_sock.sendall("                                   ")
            client_sock.sendall(send_data)

    except socket.error :   # deal with disconnect
            print "client disconnect"
            client_sock.close()

socket.close()
