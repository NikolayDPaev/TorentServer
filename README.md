# TorrentServer
Peer to peer file exchange with central server for address resolution.
Final project for the elective course Modern Java Technologies

## Functionalities
The client implements these commands
1. `register <user> <file1, file2, file3, …., fileN>` - registers which files are available at his machine
2. `unregister <user> <file1, file2, file3, …., fileN>`
3. `list-files` – lists all files that are registered at the server and the usernames of their owners.
4. `download <user> <path to file on user> <path to save>` - downloads the file from the user.

The server stores the mapping between the files and the clients ip addresses.
The client updates its local mapping from the server every 30 seconds.

The client has a mini server that handles download requests from other clients.

## How it works
1. Client A registers a file to the server
2. The server stores the ip address of client A and the name of the file
3. Client B shows to the user the addresses and the usernames in its local mapping
4. Client B requests the file from the mini server of the client A
5. The mini server of client A sends the file, one file at a time

## Diagram
![Peer-to-Peer Diagram](peer-to-peer.png)
