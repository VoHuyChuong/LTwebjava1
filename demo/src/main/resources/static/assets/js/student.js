// Dùng đường dẫn tương đối để cùng origin với Spring Boot (không cần CORS)
const API_URL = "/api/students";

let studentModal;
let currentMode = "add"; // "add" | "edit" | "view"

document.addEventListener("DOMContentLoaded", () => {
    studentModal = new bootstrap.Modal(document.getElementById("studentModal"));

    document.getElementById("btnAdd").addEventListener("click", () => openForm("add"));
    document.getElementById("studentForm").addEventListener("submit", saveStudent);
    document.getElementById("searchInput").addEventListener("input", debounce(loadStudents, 300));
    document.getElementById("studentTableBody").addEventListener("click", onTableClick);

    loadStudents();
});

/* ===================== HIỂN THỊ DANH SÁCH ===================== */

async function loadStudents() {
    const tbody = document.getElementById("studentTableBody");
    const keyword = document.getElementById("searchInput").value.trim();
    const url = keyword ? `${API_URL}?keyword=${encodeURIComponent(keyword)}` : API_URL;

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error(`Lỗi ${response.status}`);
        const students = await response.json();
        renderStudents(students);
    } catch (err) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-danger">
                    Không tải được danh sách sinh viên (${escapeHtml(err.message)}). Kiểm tra server đang chạy ở cổng 8080.
                </td>
            </tr>`;
    }
}

function renderStudents(students) {
    const tbody = document.getElementById("studentTableBody");

    if (!students.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted">Chưa có sinh viên nào.</td>
            </tr>`;
        return;
    }

    tbody.innerHTML = students.map(student => {
        const id = escapeHtml(student.id);
        const name = escapeHtml(student.fullName);
        return `
            <tr>
                <td>${escapeHtml(student.studentCode)}</td>
                <td>${name}</td>
                <td>${escapeHtml(student.email)}</td>
                <td>${escapeHtml(student.phone)}</td>
                <td>${escapeHtml(student.className)}</td>
                <td class="text-end">
                    <div class="table-actions d-flex justify-content-end gap-1">
                        <!-- Xem -->
                        <button type="button" class="btn btn-info btn-sm" title="Xem"
                                data-action="view" data-id="${id}">
                            <i class="bi bi-eye"></i>
                        </button>
                        <!-- Sửa -->
                        <button type="button" class="btn btn-warning btn-sm" title="Sửa"
                                data-action="edit" data-id="${id}">
                            <i class="bi bi-pencil-square"></i>
                        </button>
                        <!-- Xóa -->
                        <button type="button" class="btn btn-danger btn-sm" title="Xóa"
                                data-action="delete" data-id="${id}" data-name="${name}">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>`;
    }).join("");
}

function onTableClick(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) return;

    const { action, id, name } = button.dataset;
    if (action === "delete") {
        deleteStudent(id, name);
    } else {
        openForm(action, id);
    }
}

/* ===================== THÊM / SỬA / XEM ===================== */

async function openForm(mode, id = null) {
    document.getElementById("studentForm").reset();
    document.getElementById("studentId").value = "";
    setFormMode(mode);

    if (id) {
        try {
            const response = await fetch(`${API_URL}/${encodeURIComponent(id)}`);
            if (!response.ok) throw new Error(`Lỗi ${response.status}`);
            const s = await response.json();

            document.getElementById("studentId").value = s.id;
            document.getElementById("studentCode").value = s.studentCode || "";
            document.getElementById("fullName").value = s.fullName || "";
            document.getElementById("email").value = s.email || "";
            document.getElementById("phone").value = s.phone || "";
            document.getElementById("className").value = s.className || "";
        } catch (err) {
            alert("Không lấy được thông tin sinh viên: " + err.message);
            return;
        }
    }

    studentModal.show();
}

function setFormMode(mode) {
    currentMode = mode;
    const titles = {
        add: "Thêm sinh viên",
        edit: "Cập nhật sinh viên",
        view: "Thông tin sinh viên"
    };
    document.getElementById("modalTitle").innerText = titles[mode];

    const readOnly = mode === "view";
    ["studentCode", "fullName", "email", "phone", "className"].forEach(fieldId => {
        document.getElementById(fieldId).readOnly = readOnly;
    });
    document.getElementById("btnSave").classList.toggle("d-none", readOnly);
}

async function saveStudent(event) {
    event.preventDefault();
    if (currentMode === "view") return;

    const id = document.getElementById("studentId").value;
    const isEdit = currentMode === "edit" && id;

    const student = {
        studentCode: document.getElementById("studentCode").value.trim(),
        fullName: document.getElementById("fullName").value.trim(),
        email: document.getElementById("email").value.trim(),
        phone: document.getElementById("phone").value.trim(),
        className: document.getElementById("className").value.trim()
    };

    try {
        const response = await fetch(isEdit ? `${API_URL}/${encodeURIComponent(id)}` : API_URL, {
            method: isEdit ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(student)
        });
        if (!response.ok) throw new Error(`Lỗi ${response.status}`);

        studentModal.hide();
        await loadStudents();
    } catch (err) {
        alert("Không lưu được sinh viên: " + err.message);
    }
}

/* ===================== XÓA ===================== */

async function deleteStudent(id, name) {
    if (!confirm(`Bạn có chắc chắn muốn xóa sinh viên "${name}"?`)) return;

    try {
        // API DELETE trả về body rỗng nên không gọi response.json()
        const response = await fetch(`${API_URL}/${encodeURIComponent(id)}`, { method: "DELETE" });
        if (!response.ok) throw new Error(`Lỗi ${response.status}`);
        await loadStudents();
    } catch (err) {
        alert("Không xóa được sinh viên: " + err.message);
    }
}

/* ===================== TIỆN ÍCH ===================== */

// Chống chèn HTML/JS từ dữ liệu; null/undefined hiển thị rỗng thay vì chữ "null"
function escapeHtml(value) {
    if (value === null || value === undefined) return "";
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

// Đợi người dùng ngừng gõ rồi mới gọi API tìm kiếm
function debounce(fn, delay) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => fn(...args), delay);
    };
}