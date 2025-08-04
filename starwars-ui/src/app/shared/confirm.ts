import Swal from 'sweetalert2';

export function confirm(text: string): Promise<boolean> {
  return Swal.fire({
    title: 'CoNFiRMATioN',
    text,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Yes',
    cancelButtonText: 'No',
    background: '#111',
    color: '#ffe81f',
    buttonsStyling: false,
    customClass: {
      popup:          'swal-border',
      confirmButton:  'swal-btn-confirm',
      cancelButton:   'swal-btn-cancel'
    }
  }).then(r => r.isConfirmed);
}
