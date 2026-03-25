#pragma once
#include <exception>
#include <string>

class OutOfBoundsException : public std::exception {
private:
  std::string message;

public:
  OutOfBoundsException(const std::string &msg) : message(msg) {}
  const char *what() const noexcept override { return message.c_str(); }
};