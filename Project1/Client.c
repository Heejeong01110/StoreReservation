#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>

#define BUF_SIZE 1024
#define RLT_SIZE 4
#define OPSZ 4
void ErrorHandling(char* message);

int main(int argc, char* argv[]) {
	WSADATA wsaData;
	SOCKET hSocket;
	char message[BUF_SIZE];
	int result, opndCut, i, a, strLen;
	SOCKADDR_IN servAdr;

	if (argc != 3) {
		printf("Usage : %s <IP> <port> \n", argv[0]);
		exit(1);
	}

	if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0)
		ErrorHandling("WSAstartup() error");

	hSocket = socket(PF_INET, SOCK_STREAM, 0);
	if (hSocket == INVALID_SOCKET)
		ErrorHandling("socket() error");

	memset(&servAdr, 0, sizeof(servAdr));
	servAdr.sin_family = AF_INET;
	servAdr.sin_addr.s_addr = inet_addr(argv[1]);
	servAdr.sin_port = htons(atoi(argv[2]));

	if (connect(hSocket, (SOCKADDR*)&servAdr, sizeof(servAdr)) == SOCKET_ERROR) {
		ErrorHandling("connect() error");
	}
	else
		puts("Connected..............");

	while (1) {
		strLen=recv(hSocket, message, BUF_SIZE-1, 0);
		scanf("%d", &a);
		if (!strcmp(a, "4\n"))
		{
			break;
		}
		else 
		{
			send(hSocket, a, strlen(a), 0);
		}
	}
	closesocket(hSocket);
	WSACleanup();
	return 0;
}

void ErrorHandling(char* message) {
	fputs(message, stderr);
	fputc('\n', stderr);
	exit(1);
}