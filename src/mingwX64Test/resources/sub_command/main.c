#include <stdio.h>

int main() {
    int size = 1024;
    char buffer[size];
    while (fgets(buffer, size, stdin) != NULL) {
        fputs(buffer, stdout);
    }
    return 0;
}
