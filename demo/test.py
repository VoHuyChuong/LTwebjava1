import math
import random 
#1  tinh chu vi và diện tích hình chữ nhật theo chiều rộng và chiều cao nhập từ bàn phím
width=int(input("nhập chiều rộng: "))
height=int(input("nhập chiều cao: "))
chu_vi=2*(width+height)
dien_tich=width*height

print("chu vi hình chữ nhật là: ",chu_vi)
print("diện tích hình chữ nhật là: ",dien_tich)

#2 đổi đơn vị cm sang inch và dm
cm= float(input("nhập số cm: "))
inch=cm/2.54
dm=cm/100

print("số cm =",inch,"inch")
print("số cm =",dm,"dm")

#3 Sinh số ngẫu nhiên, kiểm tra 2 hay 3 chữ số
num = random.randint(10, 1000)
print("số ngẫu nhiên là: ",num)

if num >=10 and num <=99:
    print("số có 2 chữ số")
elif num >=100 and num <=999:
    print("số có 3 chữ số")
else:
    print("số kh phải 2 hay 3 chữ số")

#4 in Ngẫu nhiên [-100, 100], kiểm tra âm/dương & 2 chữ số
num = random.randint(-100, 100)
print("số ngẫu nhiên là: ",num)

if num >0:
    print("số dương")
elif num <0:
    print("số âm")
else:
    print("số 0")

so_duong = abs(num)
if so_duong >=10 and so_duong <=99:
    print("số có 2 chữ số")
else:
    print("số không có 2 chữ số")

#5 Viết chương trình tạo ngẫu nhiên một số nguyên trong khoảng [10, 150] và chuẩn hóa nó về khoảng [0, 1].
num = random.randint(10, 150)
chuan_hoa = (num - 10) / (150 - 10)
print("số ngẫu nhiên là: ",num)
print("số chuẩn hóa về khoảng [0, 1] là: ",chuan_hoa)




