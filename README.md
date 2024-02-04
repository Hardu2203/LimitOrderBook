# LimitOrderBook

A crypto currency limit order book implementation done in Kotlin using Springboot with unit tests done with junit 5. 

## Requirements

Please install java jdk 17 or later

For Ubuntu/Debian-based distributions:

```sh
sudo apt update
sudo apt install default-jdk
```

## Installation

Create security certs in used to create JWT tokens
   
1. Create a direcory called certs inside your resources directory, and execute the following commands
2. Generate private key with name keypair.pem

   ```sh
   openssl genrsa -out keypair.pem 2048
   ```
3. Extract the public key from the RSA private key file keypair.pem

   ```sh
   openssl rsa -in keypair.pem -pubout -out public.pem
   ```
4. Convert private key keypair.pem to PKCS8 format and save as private.pem
   
  ```sh
  openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem
  ```

## Usage

[Postman documentation](https://documenter.getpostman.com/view/9093373/2s9YywdeKr)

Please use the following user for testing
```sh
username: Satoshi
password: password
```
## Credits

Thanks to VALR for the challenge
