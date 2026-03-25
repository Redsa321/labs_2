#pragma once
#include <exception>
#include <string>

class InvalidRowNumberException : public std::exception {
private:
  std::string message;

public:
  InvalidRowNumberException(const std::string &msg) : message(msg) {}
  const char *what() const noexcept override { return message.c_str(); }
};